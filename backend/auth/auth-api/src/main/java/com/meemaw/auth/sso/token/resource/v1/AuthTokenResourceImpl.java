package com.meemaw.auth.sso.token.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.sso.token.model.CreateAuthTokenParams;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class AuthTokenResourceImpl implements AuthTokenResource {

  @Inject AuthTokenDatasource authTokenDatasource;
  @Inject InsightPrincipal insightPrincipal;

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = insightPrincipal.user();
    return authTokenDatasource.list(user.getId()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> create() {
    AuthUser user = insightPrincipal.user();
    CreateAuthTokenParams params =
        CreateAuthTokenParams.builder()
            .token(UUID.randomUUID().toString())
            .userId(user.getId())
            .build();

    return authTokenDatasource.create(params).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> delete(String token) {
    AuthUser user = insightPrincipal.user();
    return authTokenDatasource
        .delete(token, user.getId())
        .thenApply(deleted -> deleted ? DataResponse.ok(true) : Boom.notFound().response());
  }
}
