package com.meemaw.auth.sso.token.resource.v1;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.sso.token.model.CreateAuthTokenParams;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@Slf4j
public class AuthTokenResourceImpl implements AuthTokenResource {

  @Inject AuthTokenDatasource authTokenDatasource;
  @Inject InsightPrincipal insightPrincipal;

  @Override
  public CompletionStage<Response> me() {
    return CompletableFuture.completedStage(DataResponse.ok(insightPrincipal.user()));
  }

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = insightPrincipal.user();
    return authTokenDatasource.list(user.getId()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> create() {
    AuthUser user = insightPrincipal.user();
    String token = RandomStringUtils.randomAlphanumeric(50);
    CreateAuthTokenParams params =
        CreateAuthTokenParams.builder().token(token).userId(user.getId()).build();

    return authTokenDatasource
        .create(params)
        .thenApply(
            authToken -> {
              log.info("[AUTH]: Created auth token for user={}", user.getId());
              return DataResponse.ok(authToken);
            });
  }

  @Override
  public CompletionStage<Response> delete(String token) {
    AuthUser user = insightPrincipal.user();
    return authTokenDatasource
        .delete(token, user.getId())
        .thenApply(deleted -> deleted ? DataResponse.noContent() : Boom.notFound().response());
  }
}
