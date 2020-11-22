package com.rebrowse.model.billing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Subscription {

  String id;
  SubscriptionPlan plan;
  String organizationId;
  String status;
  String priceId;
  long currentPeriodStart;
  long currentPeriodEnd;
  OffsetDateTime createdAt;
  OffsetDateTime canceledAt;

  public static CompletionStage<Organization> retrieve(String subscriptionId) {
    return retrieve(subscriptionId, null);
  }

  public static CompletionStage<Organization> retrieve(
      String subscriptionId, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET,
        String.format("/v1/billing/subscriptions/%s", subscriptionId),
        Organization.class,
        requestOptions);
  }

  public static CompletionStage<List<Subscription>> search() {
    return search(null);
  }

  public static CompletionStage<List<Subscription>> search(RequestOptions options) {
    return search(null, options);
  }

  public static CompletionStage<List<Subscription>> search(
      SubscriptionSearchParams params, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.GET, "/v1/billing/subscriptions", params, new TypeReference<>() {}, options);
  }

  public static CompletionStage<Void> cancel(String subscriptionId) {
    return cancel(subscriptionId, null);
  }

  public static CompletionStage<Void> cancel(String subscriptionId, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.PATCH,
        String.format("/v1/billing/subscriptions/%s/cancel", subscriptionId),
        new TypeReference<>() {},
        options);
  }

  public CompletionStage<Void> cancel(RequestOptions options) {
    return cancel(id, options);
  }
}
