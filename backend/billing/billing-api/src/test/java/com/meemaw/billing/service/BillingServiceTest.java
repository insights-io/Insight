package com.meemaw.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonObject;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.customer.model.BillingCustomer;
import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.BillingSubscription;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.test.setup.AbstractAuthApiTest;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import com.stripe.net.ApiResource;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PaymentMethodCreateParams.CardDetails;
import com.stripe.param.PaymentMethodCreateParams.Type;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class BillingServiceTest extends AbstractAuthApiTest {

  @Inject BillingService billingService;
  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;

  private AuthUser testBillingUser() {
    return new UserDTO(
        UUID.randomUUID(),
        UUID.randomUUID().toString() + "@gmail.com",
        "Marko Novak",
        UserRole.STANDARD,
        Organization.identifier(),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        null,
        false);
  }

  @SneakyThrows
  private Event readStripeSubscriptionEvent(
      String path, @Nullable String customerExternalId, @Nullable String subscriptionId) {
    String payload = Files.readString(Path.of(getClass().getResource(path).toURI()));
    JsonObject event = ApiResource.GSON.fromJson(payload, JsonObject.class);
    JsonObject dataObject = event.getAsJsonObject("data").getAsJsonObject("object");
    if (customerExternalId != null) {
      dataObject.addProperty("customer", customerExternalId);
    }
    if (subscriptionId != null) {
      dataObject.addProperty("id", subscriptionId);
    }
    return ApiResource.GSON.fromJson(ApiResource.GSON.toJson(event), Event.class);
  }

  @SneakyThrows
  private Event readStripeInvoiceEvent(
      String path, @Nullable String customerExternalId, @Nullable String subscriptionId) {
    String payload = Files.readString(Path.of(getClass().getResource(path).toURI()));
    JsonObject event = ApiResource.GSON.fromJson(payload, JsonObject.class);
    JsonObject dataObject = event.getAsJsonObject("data").getAsJsonObject("object");
    if (customerExternalId != null) {
      dataObject.addProperty("customer", customerExternalId);
    }
    if (subscriptionId != null) {
      dataObject.addProperty("subscription", subscriptionId);
    }
    return ApiResource.GSON.fromJson(ApiResource.GSON.toJson(event), Event.class);
  }

  private Event readStripeInvoiceEvent(String path) {
    return readStripeInvoiceEvent(path, null, null);
  }

  private PaymentMethod createVisaTestPaymentMethod() throws StripeException {
    return PaymentMethod.create(
        PaymentMethodCreateParams.builder()
            .setType(Type.CARD)
            .setCard(
                CardDetails.builder()
                    .setNumber("4242 4242 4242 4242")
                    .setCvc("222")
                    .setExpMonth(10L)
                    .setExpYear(22L)
                    .build())
            .build());
  }

  private PaymentMethod create3DSecurePaymentMethod() throws StripeException {
    return PaymentMethod.create(
        PaymentMethodCreateParams.builder()
            .setType(Type.CARD)
            .setCard(
                CardDetails.builder()
                    .setNumber("4000 0000 0000 3220")
                    .setCvc("222")
                    .setExpMonth(10L)
                    .setExpYear(22L)
                    .build())
            .build());
  }

  @Test
  public void process_event__should_fail__when_invoice_paid_with_that_cannot_be_associated() {
    Event event = readStripeInvoiceEvent("/billing/invoice/invoicePaid.json");

    CompletionException exception =
        Assertions.assertThrows(
            CompletionException.class,
            () -> billingService.processEvent(event).toCompletableFuture().join());

    BoomException cause = (BoomException) exception.getCause();
    assertEquals(400, cause.getBoom().getStatusCode());
    assertEquals("Bad Request", cause.getBoom().getReason());
    assertEquals("Bad Request", cause.getBoom().getMessage());
  }

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

    assertNull(createSubscriptionResponse.getSubscription());
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
            "/billing/invoice/invoiceCreated3DSecure.json",
            billingSubscription.getCustomerExternalId(),
            billingSubscription.getId());

    // Process the "invoice.created" event
    billingService.processEvent(invoiceCreatedEvent).toCompletableFuture().join();
    BillingInvoice invoice =
        billingInvoiceDatasource
            .listBySubscription(billingSubscription.getId())
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
    billingService.processEvent(invoicePaidEvent).toCompletableFuture().join();
    invoice =
        billingInvoiceDatasource
            .listBySubscription(billingSubscription.getId())
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
    billingService.processEvent(subscriptionUpdatedEvent).toCompletableFuture().join();
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

    SubscriptionDTO subscription = createSubscriptionResponse.getSubscription();
    String subscriptionId = subscription.getId();
    String organizationId = subscription.getOrganizationId();

    BillingSubscription billingSubscription =
        billingSubscriptionDatasource.get(subscription.getId()).toCompletableFuture().join().get();

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
            "/billing/invoice/invoiceCreated.json", customer.getExternalId(), subscriptionId);

    // Process the "invoice.created" event
    billingService.processEvent(invoiceCreatedEvent).toCompletableFuture().join();
    BillingInvoice invoice =
        billingInvoiceDatasource
            .listBySubscription(subscriptionId)
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
    billingService.processEvent(invoicePaidEvent).toCompletableFuture().join();
    invoice = billingInvoiceDatasource.get(invoice.getId()).toCompletableFuture().join().get();

    assertEquals(1500, invoice.getAmountDue());
    assertEquals(1500, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
    assertEquals("paid", invoice.getStatus());
  }
}
