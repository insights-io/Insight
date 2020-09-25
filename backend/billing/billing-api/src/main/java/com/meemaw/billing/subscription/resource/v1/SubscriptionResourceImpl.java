package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.service.BillingService;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
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
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> create(CreateSubscriptionDTO body) {
    AuthUser user = insightPrincipal.user();
    return billingService.createSubscription(body, user).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> get() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService.getSubscription(organizationId).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> cancel() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService.cancelSubscription(organizationId).thenApply(DataResponse::ok);
  }
}
