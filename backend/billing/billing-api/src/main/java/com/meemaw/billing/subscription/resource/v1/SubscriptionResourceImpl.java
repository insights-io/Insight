package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.service.BillingService;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionDatasource;
import com.meemaw.billing.subscription.datasource.BillingSubscriptionTable;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionResourceImpl implements SubscriptionResource {

  @Inject AuthPrincipal authPrincipal;
  @Inject BillingService billingService;
  @Inject BillingSubscriptionDatasource billingSubscriptionDatasource;
  @Context HttpServerRequest request;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> retrieve(String subscriptionId) {
    String organizationId = authPrincipal.user().getOrganizationId();
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
  public CompletionStage<Response> create(CreateSubscriptionDTO body) {
    AuthUser user = authPrincipal.user();
    return billingService.createSubscription(body, user).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> list() {
    SearchDTO search =
        SearchDTO.withAllowedFields(BillingSubscriptionTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    String organizationId = authPrincipal.user().getOrganizationId();
    return billingService.searchSubscriptions(organizationId, search).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> getActivePlan() {
    String organizationId = authPrincipal.user().getOrganizationId();
    return billingService.getActivePlan(organizationId).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> cancel(String subscriptionId) {
    String organizationId = authPrincipal.user().getOrganizationId();
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
