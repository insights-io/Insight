package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.service.BillingService;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionResourceImpl implements SubscriptionResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject BillingService billingService;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> get(String subscriptionId) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingSubscriptionDatasource
        .get(subscriptionId, organizationId)
        .thenApply(
            maybeBillingSubscription -> {
              if (maybeBillingSubscription.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeBillingSubscription);
            });
  }

  @Override
  public CompletionStage<Response> createSubscription(CreateSubscriptionDTO body) {
    AuthUser user = insightPrincipal.user();
    return billingService.createSubscription(body, user).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> list() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService
        .listSubscriptionsByOrganizationId(organizationId)
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> getActivePlan() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService.getActivePlan(organizationId).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> cancelSubscription(String subscriptionId) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService
        .cancelSubscription(subscriptionId, organizationId)
        .thenApply(
            maybeCanceledSubscription -> {
              if (maybeCanceledSubscription.isEmpty()) {
                throw Boom.notFound().exception();
              }
              return DataResponse.ok(maybeCanceledSubscription.get());
            });
  }
}
