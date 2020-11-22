package com.meemaw.billing;

import com.google.gson.JsonObject;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.billing.payment.provider.stripe.StripePaymentProvider;
import com.meemaw.billing.service.stripe.StripeBillingService;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionResponseDTO;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.rebrowse.model.user.User;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import com.stripe.net.ApiResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.SneakyThrows;

public abstract class AbstractStripeTest extends ExternalAuthApiProvidedTest {

  @Inject StripeBillingService billingService;

  protected CompletionStage<CreateSubscriptionResponseDTO> createSubscription(
      User user, PaymentMethod paymentMethod) {
    return billingService.createSubscription(
        new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            UserRole.fromString(user.getRole().getKey()),
            user.getOrganizationId(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getPhoneNumber() != null
                ? new PhoneNumberDTO(
                    user.getPhoneNumber().getCountryCode(), user.getPhoneNumber().getDigits())
                : null,
            user.isPhoneNumberVerified()),
        SubscriptionPlan.ENTERPRISE,
        paymentMethod);
  }

  protected AuthUser testBillingUser() {
    return new UserDTO(
        UUID.randomUUID(),
        UUID.randomUUID().toString() + "@gmail.com",
        "Marko Novak",
        UserRole.MEMBER,
        Organization.identifier(),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        null,
        false);
  }

  @SneakyThrows
  protected Event readStripeSubscriptionEvent(
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
  protected Event readStripeInvoiceEvent(
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

  protected Event readStripeInvoiceEvent(String path) {
    return readStripeInvoiceEvent(path, null, null);
  }

  protected class MockedStripePaymentProvider extends StripePaymentProvider {

    private final PaymentMethod paymentMethod;

    public MockedStripePaymentProvider(PaymentMethod paymentMethod) {
      this.paymentMethod = paymentMethod;
    }

    @Override
    public CompletionStage<PaymentMethod> retrievePaymentMethod(String paymentMethodId) {
      return CompletableFuture.completedStage(paymentMethod);
    }
  }
}
