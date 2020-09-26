package com.meemaw.billing.webhook.service.stripe;

import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.invoice.model.CreateBillingInvoiceParams;
import com.meemaw.billing.invoice.model.UpdateBillingInvoiceParams;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.UpdateBillingSubscriptionParams;
import com.meemaw.billing.webhook.service.WebhookProcessor;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.exception.SqlException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class StripeWebhookProcessor implements WebhookProcessor<Event> {

  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Inject BillingCustomerDatasource billingCustomerDatasource;

  @ConfigProperty(name = "billing.stripe.webhook_secret")
  String stripeWebhookSecret;

  @Override
  public CompletionStage<Boolean> process(String payload, String signature) {
    try {
      Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
      return process(event);
    } catch (SignatureVerificationException ex) {
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  /**
   * Process a valid Stripe Webhook event.
   *
   * @param event stripe event as received from Webhook
   * @return boolean indicating if the event was processed
   */
  @Override
  public CompletionStage<Boolean> process(Event event) {
    switch (event.getType()) {
      case StripeWebhookEvents.INVOICE_FINALIZED:
        return handleInvoiceFinalizedEvent(event);
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
    UpdateBillingSubscriptionParams params = UpdateBillingSubscriptionParams.from(subscription);

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

  /**
   * It can happen that "invoice.paid" event comes before "invoice.finalized". In that case we try
   * to link it with an existing subscription.
   */
  private CompletionStage<Boolean> handleInvoicePaidEvent(Event event) {
    Invoice invoice = deserializeEvent(event, Invoice.class);
    UpdateBillingInvoiceParams updateParams = UpdateBillingInvoiceParams.from(invoice);

    return billingInvoiceDatasource
        .startTransaction()
        .thenCompose(
            transaction ->
                billingInvoiceDatasource
                    .update(invoice.getId(), updateParams, transaction)
                    .thenCompose(
                        maybeUpdatedBillingInvoice -> {
                          if (maybeUpdatedBillingInvoice.isEmpty()) {
                            String subscriptionId = invoice.getSubscription();
                            return billingSubscriptionDatasource
                                .get(subscriptionId, transaction)
                                .thenCompose(
                                    maybeBillingSubscription -> {
                                      if (maybeBillingSubscription.isEmpty()) {
                                        log.error(
                                            "[BILLING]: Failed to associate \"invoice.paid\" event with subscription event={}",
                                            event);
                                        throw Boom.badRequest().exception();
                                      }

                                      String organizationId =
                                          maybeBillingSubscription.get().getCustomerInternalId();
                                      CreateBillingInvoiceParams createParams =
                                          CreateBillingInvoiceParams.from(invoice, organizationId);

                                      return billingInvoiceDatasource
                                          .create(createParams, transaction)
                                          .thenApply(
                                              billingInvoice -> {
                                                log.info(
                                                    "[BILLING]: Successfully created new invoice after linking \"invoice.paid\" with existing subscription");
                                                return true;
                                              });
                                    });
                          }

                          log.info(
                              "[BILLING]: Successfully updated existing invoice on \"invoice.paid\" event");
                          return CompletableFuture.completedStage(true);
                        })
                    .thenCompose(result -> transaction.commit().thenApply(ignored -> result)));
  }

  /**
   * It can happen that "invoice.paid" event comes before "invoice.finalized". In that case we will
   * get a unique constraint violation on "invoice_pkey" which we can just catch & ignore not to
   * spam error logs.
   */
  private CompletionStage<Boolean> handleInvoiceFinalizedEvent(Event event) {
    Invoice invoice = deserializeEvent(event, Invoice.class);
    return billingInvoiceDatasource
        .startTransaction()
        .thenCompose(
            transaction ->
                billingCustomerDatasource
                    .getByExternalId(invoice.getCustomer(), transaction)
                    .thenCompose(
                        maybeBillingCustomer -> {
                          if (maybeBillingCustomer.isEmpty()) {
                            log.error(
                                "[BILLING]: Failed to associate invoice with customer event={}",
                                invoice);
                            throw Boom.badRequest().exception();
                          }

                          String organizationId = maybeBillingCustomer.get().getInternalId();
                          CreateBillingInvoiceParams createParams =
                              CreateBillingInvoiceParams.from(invoice, organizationId);
                          MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
                          return billingInvoiceDatasource.create(createParams, transaction);
                        })
                    .thenCompose(
                        billingInvoice ->
                            transaction
                                .commit()
                                .thenApply(
                                    ignored -> {
                                      log.info(
                                          "[BILLING] Successfully created billing invoice={}",
                                          billingInvoice);
                                      return true;
                                    })))
        .exceptionally(
            throwable -> {
              SqlException cause = (SqlException) throwable.getCause();
              if (cause.hadConflict()) {
                log.info(
                    "[BILLING]: Conflict on \"invoice.finalized\" event invoice={}",
                    invoice.getId());
                return true;
              }

              throw cause;
            });
  }

  private <T> T deserializeEvent(Event event, Class<T> clazz) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    if (dataObjectDeserializer.getObject().isEmpty()) {
      log.error("[BILLING]: Failed to deserialize event={} to class={}", event, clazz);
      throw Boom.badRequest().message("[BILLING]: Failed to deserialize event").exception();
    }
    return (T) dataObjectDeserializer.getObject().get();
  }
}
