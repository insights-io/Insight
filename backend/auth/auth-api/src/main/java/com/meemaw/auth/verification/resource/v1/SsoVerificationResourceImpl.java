package com.meemaw.auth.verification.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.TfaClientId;
import com.meemaw.auth.sso.model.TfaCompleteDTO;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.verification.service.TfaService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoVerificationResourceImpl implements SsoVerificationResource {

  @Inject InsightPrincipal principal;
  @Inject TfaService tfaService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> tfaSetupStart() {
    AuthUser user = principal.user();
    return tfaService.tfaSetupStart(user.getId(), user.getEmail()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> tfaSetupComplete(TfaCompleteDTO body) {
    AuthUser user = principal.user();
    return tfaService
        .tfaSetupComplete(user.getId(), body.getCode())
        .thenApply(tfaSetup -> DataResponse.ok(true));
  }

  @Override
  public CompletionStage<Response> tfaComplete(String tfaClientId, TfaCompleteDTO body) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaService
        .tfaComplete(body.getCode(), tfaClientId)
        .thenApply(
            sessionId ->
                SsoSession.cookieResponseBuilder(sessionId, cookieDomain)
                    .cookie(TfaClientId.clearCookie(cookieDomain))
                    .build());
  }
}
