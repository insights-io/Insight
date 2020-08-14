package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.sso.service.SsoTfaService;
import com.meemaw.auth.sso.service.exception.VerificationSessionExpiredException;
import com.meemaw.shared.context.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoVerificationResourceImpl implements SsoVerificationResource {

  @Inject SsoTfaService ssoTfaService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> complete(String verificationId, TfaCompleteDTO body) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoTfaService
        .tfaComplete(body.getCode(), verificationId)
        .thenApply(
            sessionId ->
                SsoSession.cookieResponseBuilder(sessionId, cookieDomain)
                    .cookie(SsoVerification.clearCookie(cookieDomain))
                    .build())
        .exceptionally(
            throwable -> {
              if (throwable.getCause() instanceof VerificationSessionExpiredException) {
                return ((VerificationSessionExpiredException) throwable.getCause())
                    .response(cookieDomain);
              }

              throw (RuntimeException) throwable;
            });
  }
}
