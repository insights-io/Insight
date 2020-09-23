package com.meemaw.billing.service;

import static com.meemaw.shared.SharedConstants.INSIGHT_ORGANIZATION_ID;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import com.meemaw.billing.payment.provider.PaymentProvider;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.CreateBillingSubscriptionParams;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.PriceDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
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

  public CompletionStage<SubscriptionDTO> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user) {

    return paymentProvider
        .retrievePaymentMethod(createSubscription.getPaymentMethodId())
        .thenCompose(
            paymentMethod -> createSubscription(user, createSubscription.getPlan(), paymentMethod));
  }

  public CompletionStage<SubscriptionDTO> createSubscription(
      AuthUser user, SubscriptionPlan plan, PaymentMethod paymentMethod) {
    String paymentMethodId = paymentMethod.getId();
    String organizationId = user.getOrganizationId();
    String email = user.getOrganizationId();

    String priceId =
        Optional.ofNullable(subscriptionPriceLookup.get(plan))
            .orElseThrow(() -> Boom.badRequest().message("Invalid plan").exception());

    log.info(
        "[AUTH]: Create subscription attempt email={} organization={} priceId={} plan={} paymentMethodId={}",
        email,
        organizationId,
        priceId,
        plan,
        paymentMethodId);

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
                                                      "[AUTH]: Created new customer for organizationId={} customerId={}",
                                                      organizationId,
                                                      customer.getId());
                                                  return customer;
                                                }));
                          }

                          return paymentProvider.retrieveCustomer(
                              maybeBillingCustomer.get().getExternalId());
                        })
                    .thenCompose(customer -> createSubscription(customer.getId(), priceId))
                    .thenCompose(
                        subscription -> {
                          String customerId = subscription.getCustomer();

                          CreateBillingSubscriptionParams params =
                              new CreateBillingSubscriptionParams(
                                  subscription.getId(),
                                  plan,
                                  customerId,
                                  organizationId,
                                  priceId,
                                  subscription.getCurrentPeriodEnd());

                          return billingSubscriptionDatasource
                              .create(params)
                              .thenApply(
                                  billingSubscription -> {
                                    log.info(
                                        "[AUTH]: Created new billing subscription organizationId={} customerId={} priceId={}",
                                        organizationId,
                                        customerId,
                                        priceId);

                                    return new SubscriptionDTO(
                                        billingSubscription.getId(),
                                        billingSubscription.getCustomerInternalId(),
                                        billingSubscription.getPlan(),
                                        PriceDTO.fromStripe(price),
                                        billingSubscription.getCreatedAt());
                                  });
                        }));
  }

  private CompletionStage<Subscription> createSubscription(String customerId, String priceId) {
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
      case "invoice.paid":
        return handleInvoicePaid(event);
      default:
        log.warn("[AUTH]: Unhandled billing event type={}", event.getType());
        return CompletableFuture.completedStage(false);
    }
  }

  private CompletionStage<Boolean> handleInvoicePaid(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    if (dataObjectDeserializer.getObject().isEmpty()) {
      log.error("[AUTH]: Failed to deserialize invoice event={}", event);
      throw Boom.badRequest().message("[AUTH]: Failed to deserialize invoice").exception();
    }

    Invoice invoice = (Invoice) dataObjectDeserializer.getObject().get();
    return billingCustomerDatasource
        .getByExternalId(invoice.getCustomer())
        .thenCompose(
            maybeBillingCustomer -> {
              if (maybeBillingCustomer.isEmpty()) {
                log.error(
                    "[AUTH]: Failed to associate invoice payment with organization event={}",
                    event);
                throw Boom.badRequest().exception();
              }
              String organizationId = maybeBillingCustomer.get().getInternalId();
              MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

              CreateBillingInvoiceParams params =
                  new CreateBillingInvoiceParams(
                      invoice.getId(),
                      invoice.getSubscription(),
                      invoice.getCurrency(),
                      invoice.getAmountPaid());

              return billingInvoiceDatasource.create(params);
            })
        .thenApply(
            billingInvoice -> {
              log.info("[AUTH] Successfully created billing invoice={}", billingInvoice);
              return true;
            });
  }

  public CompletionStage<SubscriptionDTO> getSubscription(String organizationId) {
    if (INSIGHT_ORGANIZATION_ID.equals(organizationId)) {
      return CompletableFuture.completedStage(SubscriptionDTO.insight());
    }

    return billingSubscriptionDatasource
        .findByCustomerInternalId(organizationId)
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
