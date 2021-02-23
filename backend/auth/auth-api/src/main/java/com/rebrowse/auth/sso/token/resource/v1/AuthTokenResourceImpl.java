package com.rebrowse.auth.sso.token.resource.v1;

import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.sso.token.datasource.AuthTokenDatasource;
import com.rebrowse.auth.sso.token.model.CreateAuthTokenParams;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
public class AuthTokenResourceImpl implements AuthTokenResource {

  @Inject AuthTokenDatasource authTokenDatasource;
  @Inject AuthPrincipal authPrincipal;

  @Override
  public CompletionStage<Response> me() {
    return CompletableFuture.completedStage(DataResponse.ok(authPrincipal.user()));
  }

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = authPrincipal.user();
    return authTokenDatasource.list(user.getId()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> create() {
    AuthUser user = authPrincipal.user();
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
    AuthUser user = authPrincipal.user();
    return authTokenDatasource
        .delete(token, user.getId())
        .thenApply(deleted -> deleted ? DataResponse.noContent() : Boom.notFound().response());
  }
}
