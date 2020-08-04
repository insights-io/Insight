package com.meemaw.session.insights.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class InsightsResourceImpl implements InsightsResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject SessionDatasource sessionDatasource;

  @Override
  public CompletionStage<Response> byCountry() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return sessionDatasource.countByCountries(organizationId).thenApply(DataResponse::ok);
  }
}
