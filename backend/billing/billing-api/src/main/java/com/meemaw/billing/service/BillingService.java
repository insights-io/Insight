package com.meemaw.billing.service;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import com.meemaw.billing.invoice.model.UpdateBillingInvoiceParams;
import com.meemaw.billing.payment.provider.PaymentProvider;
import com.meemaw.billing.payment.provider.stripe.StripeWebhookEvents;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.billing.subscription.model.dto.PriceDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class BillingService {

  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Inject PaymentProvider paymentProvider;

  @ConfigProperty(name = "billing.stripe.business_plan.price_id")
  String businessPlanPriceId;

  @ConfigProperty(name = "billing.stripe.enterprise_plan.price_id")
  String enterprisePlanPriceId;

  private Map<SubscriptionPlan, String> subscriptionPriceLookup;

  @PostConstruct
  public void init() {
    subscriptionPriceLookup = new HashMap<>(2);
    subscriptionPriceLookup.put(SubscriptionPlan.BUSINESS, businessPlanPriceId);
    subscriptionPriceLookup.put(SubscriptionPlan.ENTERPRISE, enterprisePlanPriceId);
  }

  public CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user) {

    return paymentProvider
        .retrievePaymentMethod(createSubscription.getPaymentMethodId())
        .thenCompose(
            paymentMethod -> createSubscription(user, createSubscription.getPlan(), paymentMethod));
  }

  public CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      AuthUser user, SubscriptionPlan plan, PaymentMethod paymentMethod) {
    String organizationId = user.getOrganizationId();
    String priceId =
        Optional.ofNullable(subscriptionPriceLookup.get(plan))
            .orElseThrow(() -> Boom.badRequest().message("Invalid plan").exception());

    return paymentProvider
        .getPrice(priceId)
        .thenCompose(
            price ->
                billingCustomerDatasource
                    .getByInternalId(organizationId)
                    .thenCompose(
                        maybeBillingCustomer -> {
                          if (maybeBillingCustomer.isEmpty()) {
                            return createCustomer(user, paymentMethod)
                                .thenCompose(
                                    customer ->
                                        billingCustomerDatasource
                                            .create(customer.getId(), organizationId)
                                            .thenApply(
                                                ignored -> {
                                                  log.info(
                                                      "[BILLING]: Created new customer for organizationId={} customerId={}",
                                                      organizationId,
                                                      customer.getId());
                                                  return customer;
                                                }));
                          }

                          return paymentProvider.retrieveCustomer(
                              maybeBillingCustomer.get().getExternalId());
                        })
                    .thenCompose(
                        customer ->
                            createStripeSubscription(customer.getId(), priceId)
                                .thenCompose(
                                    subscription -> {
                                      PaymentIntent paymentIntent =
                                          subscription
                                              .getLatestInvoiceObject()
                                              .getPaymentIntentObject();

                                      CreateBillingSubscriptionParams params =
                                          new CreateBillingSubscriptionParams(
                                              subscription.getId(),
                                              plan,
                                              customer.getId(),
                                              organizationId,
                                              subscription.getStatus(),
                                              priceId,
                                              subscription.getCurrentPeriodStart(),
                                              subscription.getCurrentPeriodEnd());

                                      return billingSubscriptionDatasource
                                          .create(params)
                                          .thenApply(
                                              billingSubscription -> {
                                                SubscriptionDTO subscriptionDTO =
                                                    new SubscriptionDTO(
                                                        billingSubscription.getId(),
                                                        billingSubscription.getCustomerInternalId(),
                                                        billingSubscription.getPlan(),
                                                        PriceDTO.fromStripe(price),
                                                        billingSubscription.getCreatedAt());

                                                return CreateSubscriptionResponseDTO.create(
                                                    subscriptionDTO, paymentIntent);
                                              });
                                    })));
  }

  private CompletionStage<Subscription> createStripeSubscription(
      String customerId, String priceId) {
    SubscriptionCreateParams params =
        SubscriptionCreateParams.builder()
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setCustomer(customerId)
            .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
            .build();

    return paymentProvider.createSubscription(params);
  }

  CompletionStage<Customer> createCustomer(AuthUser user, PaymentMethod paymentMethod) {
    String phoneNumber =
        Optional.ofNullable(user.getPhoneNumber()).map(PhoneNumber::getNumber).orElse(null);

    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setDescription("Organization: " + user.getOrganizationId())
            .setEmail(user.getEmail())
            .setPhone(phoneNumber)
            .build();

    return paymentProvider
        .createCustomer(params)
        .thenCompose(
            customer ->
                paymentProvider
                    .attach(
                        paymentMethod,
                        PaymentMethodAttachParams.builder().setCustomer(customer.getId()).build())
                    .thenApply(ignored -> customer))
        .thenCompose(
            customer ->
                paymentProvider.updateCustomer(
                    customer,
                    CustomerUpdateParams.builder()
                        .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethod.getId())
                                .build())
                        .build()));
  }

  /**
   * Process Stripe Webhook event
   *
   * @param event stripe event as received from Webhook
   * @return boolean indicating if the event was processed
   */
  public CompletionStage<Boolean> processEvent(Event event) {
    switch (event.getType()) {
      case StripeWebhookEvents.INVOICE_CREATED:
        return handleInvoiceCreatedEvent(event);
      case StripeWebhookEvents.INVOICE_PAID:
        return handleInvoicePaidEvent(event);
      case StripeWebhookEvents.SUBSCRIPTION_UPDATED:
        return handleSubscriptionUpdated(event);
      case StripeWebhookEvents.PAYMENT_INTENT_PAYMENT_FAILED:
        return handlePaymentIntentPaymentFailed(event);
      default:
        return CompletableFuture.completedStage(false);
    }
  }

  private <T> T deserializeEvent(Event event, Class<T> clazz) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    if (dataObjectDeserializer.getObject().isEmpty()) {
      log.error("[BILLING]: Failed to deserialize event={} to class={}", event, clazz);
      throw Boom.badRequest().message("[BILLING]: Failed to deserialize event").exception();
    }
    return (T) dataObjectDeserializer.getObject().get();
  }

  private CompletionStage<Boolean> handlePaymentIntentPaymentFailed(Event event) {
    PaymentIntent paymentIntent = deserializeEvent(event, PaymentIntent.class);
    String invoiceId = paymentIntent.getInvoice();

    return billingInvoiceDatasource
        .get(invoiceId)
        .thenCompose(
            maybeBillingInvoice -> {
              if (maybeBillingInvoice.isEmpty()) {
                log.error(
                    "[BILLING]: Failed to associate failed payment intent with invoice event={}",
                    event);
                throw Boom.badRequest().exception();
              }

              BillingInvoice billingInvoice = maybeBillingInvoice.get();
              String subscriptionId = billingInvoice.getSubscriptionId();
              if ("Subscription creation".equals(paymentIntent.getDescription())) {
                log.info("[BILLING]: Invoice payment failed on create subscription - deleting ...");
                return billingSubscriptionDatasource.delete(subscriptionId);
              }

              return CompletableFuture.completedStage(true);
            });
  }

  private CompletionStage<Boolean> handleSubscriptionUpdated(Event event) {
    Subscription subscription = deserializeEvent(event, Subscription.class);
    UpdateBillingSubscriptionParams params =
        UpdateBillingSubscriptionParams.builder()
            .status(subscription.getStatus())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .build();

    return billingSubscriptionDatasource
        .update(subscription.getId(), params)
        .thenApply(
            maybeBillingSubscription -> {
              if (maybeBillingSubscription.isEmpty()) {
                log.error(
                    "[BILLING]: Failed to associate updated subscription with an existing one event={}",
                    event);
                throw Boom.badRequest().exception();
              }
              BillingSubscription billingSubscription = maybeBillingSubscription.get();
              log.info(
                  "[BILLING]: Successfully updated billing subscription={}", billingSubscription);
              return true;
            });
  }

  private CompletionStage<Boolean> handleInvoicePaidEvent(Event event) {
    Invoice invoice = deserializeEvent(event, Invoice.class);
    UpdateBillingInvoiceParams params =
        UpdateBillingInvoiceParams.builder()
            .status(invoice.getStatus())
            .amountPaid(invoice.getAmountPaid())
            .amountDue(invoice.getAmountDue())
            .build();

    return billingInvoiceDatasource
        .update(invoice.getId(), params)
        .thenApply(
            maybeBillingInvoice -> {
              if (maybeBillingInvoice.isEmpty()) {
                log.error("[BILLING]: Failed to find paid invoice={}", event);
                throw Boom.badRequest().exception();
              }
              BillingInvoice billingInvoice = maybeBillingInvoice.get();
              log.info("[BILLING]: Successfully updated billing invoice={}", billingInvoice);
              return true;
            });
  }

  private CompletionStage<Boolean> handleInvoiceCreatedEvent(Event event) {
    Invoice invoice = deserializeEvent(event, Invoice.class);
    return billingCustomerDatasource
        .getByExternalId(invoice.getCustomer())
        .thenCompose(
            maybeBillingCustomer -> {
              if (maybeBillingCustomer.isEmpty()) {
                log.error("[BILLING]: Failed to associate invoice with customer event={}", invoice);
                throw Boom.badRequest().exception();
              }

              String organizationId = maybeBillingCustomer.get().getInternalId();
              MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

              CreateBillingInvoiceParams params =
                  new CreateBillingInvoiceParams(
                      invoice.getId(),
                      invoice.getSubscription(),
                      invoice.getPaymentIntent(),
                      invoice.getCurrency(),
                      invoice.getAmountPaid(),
                      invoice.getAmountDue(),
                      invoice.getStatus());

              return billingInvoiceDatasource.create(params);
            })
        .thenApply(
            billingInvoice -> {
              log.info("[BILLING] Successfully created billing invoice={}", billingInvoice);
              return true;
            });
  }

  public CompletionStage<SubscriptionDTO> getSubscription(String organizationId) {
    if (INSIGHT_ORGANIZATION_ID.equals(organizationId)) {
      return CompletableFuture.completedStage(SubscriptionDTO.insight());
    }

    return billingSubscriptionDatasource
        .getByCustomerInternalId(organizationId)
        .thenCompose(
            maybeSubscription -> {
              if (maybeSubscription.isEmpty()) {
                return CompletableFuture.completedStage(SubscriptionDTO.free(organizationId));
              }

              BillingSubscription subscription = maybeSubscription.get();
              return paymentProvider
                  .getPrice(subscription.getPriceId())
                  .thenApply(
                      price ->
                          new SubscriptionDTO(
                              subscription.getId(),
                              organizationId,
                              subscription.getPlan(),
                              PriceDTO.fromStripe(price),
                              subscription.getCreatedAt()));
            });
  }
}
