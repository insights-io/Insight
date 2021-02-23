package com.rebrowse.auth.mfa.setup.resource.v1;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.setup.service.MfaSetupService;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class MfaSetupResourceImpl implements MfaSetupResource {

  @Inject AuthPrincipal principal;
  @Inject MfaSetupService mfaSetupService;
  @Inject
  UserMfaDatasource userMfaDatasource;
  @Context HttpServerRequest request;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> complete(MfaMethod method, MfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    String sessionId = principal.sessionId();
    return mfaSetupService
        .completeSetup(sessionId, method, user, body.getCode())
        .thenApply(data -> DataResponse.ok(data.getLeft().dto()));
  }

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = principal.user();
    return userMfaDatasource
        .list(user.getId())
        .thenApply(
            setups -> setups.stream().map(MfaConfiguration::dto).collect(Collectors.toList()))
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> retrieve(MfaMethod method) {
    AuthUser user = principal.user();
    return userMfaDatasource
        .retrieve(user.getId(), method)
        .thenApply(
            maybeConfiguration -> {
              if (maybeConfiguration.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeConfiguration.get().dto());
            });
  }

  @Override
  public CompletionStage<Response> delete(MfaMethod method) {
    AuthUser user = principal.user();
    return mfaSetupService
        .mfaSetupDisable(user.getId(), method)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> start(MfaMethod method) {
    AuthUser user = principal.user();
    String sessionId = principal.sessionId();
    return mfaSetupService.startSetup(sessionId, method, user).thenApply(DataResponse::ok);
  }
}
