package com.meemaw.billing.service;

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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class BillingService {

  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Inject PaymentProvider paymentProvider;

  String priceId = "price_1HRjvtI1ysvdCIIxwuxy4FEI";

  public CompletionStage<BillingSubscription> createSubscription(
      CreateSubscriptionDTO createSubscription, AuthUser user) {
    return paymentProvider
        .retrievePaymentMethod(createSubscription.getPaymentMethodId())
        .thenCompose(paymentMethod -> createSubscription(user, paymentMethod));
  }

  public CompletionStage<BillingSubscription> createSubscription(
      AuthUser user, PaymentMethod paymentMethod) {
    String paymentMethodId = paymentMethod.getId();
    String organizationId = user.getOrganizationId();
    String email = user.getOrganizationId();

    log.info(
        "[AUTH]: Create subscription attempt email={} organization={} paymentMethodId={}",
        email,
        organizationId,
        paymentMethodId);

    return billingCustomerDatasource
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

              return paymentProvider.retrieveCustomer(maybeBillingCustomer.get().getExternalId());
            })
        .thenCompose(customer -> createSubscription(customer.getId(), priceId))
        .thenCompose(
            subscription -> {
              String priceId = subscription.getItems().getData().get(0).getPrice().getId();
              String customerId = subscription.getCustomer();

              CreateBillingSubscriptionParams params =
                  new CreateBillingSubscriptionParams(
                      subscription.getId(),
                      SubscriptionPlan.ENTERPRISE,
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

                        return billingSubscription;
                      });
            });
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
}
