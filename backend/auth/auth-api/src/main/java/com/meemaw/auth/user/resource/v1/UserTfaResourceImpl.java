package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.sso.service.SsoTfaService;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

public class UserTfaResourceImpl implements UserTfaResource {

  @Inject InsightPrincipal principal;
  @Inject SsoTfaService ssoTfaService;
  @Inject UserTfaDatasource userTfaDatasource;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> get() {
    AuthUser user = principal.user();
    return userTfaDatasource
        .get(user.getId())
        .thenApply(
            maybeTfaSetup -> {
              if (maybeTfaSetup.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeTfaSetup.get().dto());
            });
  }

  @Override
  public CompletionStage<Response> tfaSetupStart() {
    AuthUser user = principal.user();
    return ssoTfaService.tfaSetupStart(user.getId(), user.getEmail()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> tfaSetupComplete(TfaCompleteDTO body) {
    AuthUser user = principal.user();
    return ssoTfaService
        .tfaSetupComplete(user.getId(), body.getCode())
        .thenApply(tfaSetup -> DataResponse.ok(tfaSetup.dto()));
  }

  @Override
  public CompletionStage<Response> tfaSetupDisable() {
    AuthUser user = principal.user();
    return ssoTfaService.tfaSetupDisable(user.getId()).thenApply(DataResponse::ok);
  }
}
