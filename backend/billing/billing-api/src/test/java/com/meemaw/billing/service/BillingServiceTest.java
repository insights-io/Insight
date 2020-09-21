package com.meemaw.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.billing.customer.datasource.BillingCustomerDatasource;
import com.meemaw.billing.customer.model.BillingCustomer;
import com.meemaw.billing.invoice.datasource.BillingInvoiceDatasource;
import com.meemaw.billing.invoice.model.BillingInvoice;
import com.meemaw.billing.subscription.model.BillingSubscription;
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

  @SneakyThrows
  private Event readStripeEvent(
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

  private Event readStripeEvent(String path) {
    return readStripeEvent(path, null, null);
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

  @Test
  public void process_event__should_fail__when_invoice_paid_with_that_cannot_be_associated() {
    Event event = readStripeEvent("/billing/invoice/invoicePaid.json");

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
  public void process_event__should_create_invoice_on_subscription__when_invoice_paid()
      throws StripeException {
    AuthUser user =
        new UserDTO(
            UUID.randomUUID(),
            UUID.randomUUID().toString() + "@gmail.com",
            "Marko Novak",
            UserRole.STANDARD,
            Organization.identifier(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null,
            false);

    // Create new BillingSubscription
    PaymentMethod paymentMethod = createVisaTestPaymentMethod();
    BillingSubscription subscription =
        billingService.createSubscription(user, paymentMethod).toCompletableFuture().join();

    String subscriptionId = subscription.getId();
    String organizationId = subscription.getCustomerInternalId();

    // Fetch the customer that was created for subscription
    BillingCustomer customer =
        billingCustomerDatasource
            .getByInternalId(organizationId)
            .toCompletableFuture()
            .join()
            .get();

    Event event =
        readStripeEvent(
            "/billing/invoice/invoicePaid.json", customer.getExternalId(), subscriptionId);

    // Process the event
    billingService.processEvent(event).toCompletableFuture().join();

    BillingInvoice invoice =
        billingInvoiceDatasource
            .listBySubscription(subscriptionId)
            .toCompletableFuture()
            .join()
            .get(0);

    assertEquals(1500, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
  }
}
