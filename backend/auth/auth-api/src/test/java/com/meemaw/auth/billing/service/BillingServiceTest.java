package com.meemaw.auth.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.meemaw.auth.billing.datasource.BillingCustomerDatasource;
import com.meemaw.auth.billing.datasource.BillingInvoiceDatasource;
import com.meemaw.auth.billing.model.BillingCustomer;
import com.meemaw.auth.billing.model.BillingInvoice;
import com.meemaw.auth.billing.model.BillingSubscription;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import com.stripe.net.ApiResource;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PaymentMethodCreateParams.CardDetails;
import com.stripe.param.PaymentMethodCreateParams.Type;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class BillingServiceTest {

  @Inject BillingService billingService;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject BillingCustomerDatasource billingCustomerDatasource;
  @Inject BillingInvoiceDatasource billingInvoiceDatasource;
  @Inject UserDatasource userDatasource;

  @SneakyThrows
  private Event readStripeEvent(
      String path, @Nullable String customerId, @Nullable String subscriptionId) {
    String payload = Files.readString(Path.of(getClass().getResource(path).toURI()));
    JsonObject event = ApiResource.GSON.fromJson(payload, JsonObject.class);
    JsonObject dataObject = event.getAsJsonObject("data").getAsJsonObject("object");
    if (customerId != null) {
      dataObject.addProperty("customer", customerId);
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
      throws IOException, StripeException {
    String password = UUID.randomUUID().toString();
    String email = password + "@gmail.com";
    SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);
    AuthUser user = userDatasource.findUser(email).toCompletableFuture().join().get();

    // Create new BillingSubscription
    PaymentMethod paymentMethod = createVisaTestPaymentMethod();
    BillingSubscription subscription =
        billingService.createSubscription(user, paymentMethod).toCompletableFuture().join();

    // Fetch the customer that was created for subscription
    BillingCustomer customer =
        billingCustomerDatasource
            .findByOrganization(subscription.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get();

    Event event =
        readStripeEvent(
            "/billing/invoice/invoicePaid.json", customer.getId(), subscription.getId());

    // Process the event
    billingService.processEvent(event).toCompletableFuture().join();

    BillingInvoice invoice =
        billingInvoiceDatasource
            .listByOrganizationId(user.getOrganizationId())
            .toCompletableFuture()
            .join()
            .get(0);

    assertEquals(1500, invoice.getAmountPaid());
    assertEquals("usd", invoice.getCurrency());
  }
}
