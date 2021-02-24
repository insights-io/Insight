package com.rebrowse.billing.service.stripe;

import static com.rebrowse.billing.utils.BillingTestUtils.create3DSecurePaymentMethod;
import static com.rebrowse.billing.utils.BillingTestUtils.createVisaTestPaymentMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.billing.customer.datasource.BillingCustomerDatasource;
import com.rebrowse.billing.customer.model.BillingCustomer;
import com.rebrowse.billing.invoice.datasource.BillingInvoiceDatasource;
import com.rebrowse.billing.invoice.model.BillingInvoice;
import com.rebrowse.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.rebrowse.billing.subscription.model.BillingSubscription;
import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.rebrowse.billing.subscription.model.dto.PlanDTO;
import com.rebrowse.billing.utils.AbstractStripeQuarkusTest;
import com.rebrowse.billing.webhook.service.WebhookProcessor;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

// TODO: rewrite tests to use resource only (mock signature verification)
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class StripeBillingServiceTest extends AbstractStripeQuarkusTest {

  @Inject WebhookProcessor<Event> webhookProcessor;
  @Inject StripeBillingService billingService;
  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;

  @Test
  public void process_event__should_successfully_process_invoice_events__on_3d_secure_payment()
      throws StripeException {
    AuthUser user = testBillingUser();

    // Fake client payment flow
    PaymentMethod threedSecurePaymentMethod = create3DSecurePaymentMethod();
    CreateSubscriptionResponseDTO createSubscriptionResponse =
        billingService
            .createSubscription(user, SubscriptionPlan.ENTERPRISE, threedSecurePaymentMethod)
            .toCompletableFuture()
            .join();

    assertNull(createSubscriptionResponse.getPlan());
    assertNotNull(createSubscriptionResponse.getClientSecret());

    BillingSubscription billingSubscription =
        billingSubscriptionDatasource
            .getByCustomerInternalId(user.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get();

    assertEquals("incomplete", billingSubscription.getStatus());

    // Link example payload with current subscription
    Event invoiceCreatedEvent =
        readStripeInvoiceEvent(
            "/billing/invoice/invoiceFinalized3DSecure.json",
            billingSubscription.getCustomerExternalId(),
            billingSubscription.getId());

    // Process the "invoice.finalized" event
    webhookProcessor.process(invoiceCreatedEvent).toCompletableFuture().join();

    BillingInvoice invoice =
        billingInvoiceDatasource
            .listBySubscription(billingSubscription.getId(), user.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get(0);

    assertEquals(1500, invoice.getAmountDue());
    assertEquals(0, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
    assertEquals("open", invoice.getStatus());

    // Link example payload with current subscription
    Event invoicePaidEvent =
        readStripeInvoiceEvent(
            "/billing/invoice/invoicePaid3DSecure.json",
            billingSubscription.getCustomerExternalId(),
            billingSubscription.getId());

    // Process the "invoice.paid" event
    webhookProcessor.process(invoicePaidEvent).toCompletableFuture().join();
    invoice =
        billingInvoiceDatasource
            .listBySubscription(billingSubscription.getId(), user.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get(0);

    assertEquals(1500, invoice.getAmountDue());
    assertEquals(1500, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
    assertEquals("paid", invoice.getStatus());

    // Link example payload with current subscription
    Event subscriptionUpdatedEvent =
        readStripeSubscriptionEvent(
            "/billing/invoice/subscriptionUpdated3DSecure.json",
            billingSubscription.getCustomerExternalId(),
            billingSubscription.getId());

    // Process the "customer.subscription.updated" event
    webhookProcessor.process(subscriptionUpdatedEvent).toCompletableFuture().join();
    billingSubscription =
        billingSubscriptionDatasource
            .getByCustomerInternalId(user.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get();

    assertEquals("active", billingSubscription.getStatus());
  }

  @Test
  public void process_event__should_successfully_process_invoice_events__when_visa_payment()
      throws StripeException {
    AuthUser user = testBillingUser();

    // Fake client payment flow
    PaymentMethod visaTestPaymentMethod = createVisaTestPaymentMethod();
    CreateSubscriptionResponseDTO createSubscriptionResponse =
        billingService
            .createSubscription(user, SubscriptionPlan.ENTERPRISE, visaTestPaymentMethod)
            .toCompletableFuture()
            .join();

    assertNull(createSubscriptionResponse.getClientSecret());

    PlanDTO plan = createSubscriptionResponse.getPlan();
    String subscriptionId = plan.getSubscriptionId();
    String organizationId = plan.getOrganizationId();

    BillingSubscription billingSubscription =
        billingSubscriptionDatasource
            .get(plan.getSubscriptionId())
            .toCompletableFuture()
            .join()
            .get();

    assertEquals("active", billingSubscription.getStatus());

    // Fetch the customer that was created for subscription
    BillingCustomer customer =
        billingCustomerDatasource
            .getByInternalId(organizationId)
            .toCompletableFuture()
            .join()
            .get();

    // Link example payload with current subscription
    Event invoiceCreatedEvent =
        readStripeInvoiceEvent(
            "/billing/invoice/invoiceFinalized.json", customer.getExternalId(), subscriptionId);

    // Process the "invoice.created" event
    webhookProcessor.process(invoiceCreatedEvent).toCompletableFuture().join();
    BillingInvoice invoice =
        billingInvoiceDatasource
            .listBySubscription(subscriptionId, organizationId)
            .toCompletableFuture()
            .join()
            .get(0);

    assertEquals(1500, invoice.getAmountDue());
    assertEquals(0, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
    assertEquals("open", invoice.getStatus());

    // Link example payload with current subscription
    Event invoicePaidEvent =
        readStripeInvoiceEvent(
            "/billing/invoice/invoicePaid.json", customer.getExternalId(), subscriptionId);

    // Process the "invoice.paid" event
    webhookProcessor.process(invoicePaidEvent).toCompletableFuture().join();
    invoice = billingInvoiceDatasource.get(invoice.getId()).toCompletableFuture().join().get();

    assertEquals(1500, invoice.getAmountDue());
    assertEquals(1500, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
    assertEquals("paid", invoice.getStatus());
  }
}
