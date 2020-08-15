package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.sso.service.SsoTfaService;
import com.meemaw.auth.sso.service.exception.VerificationSessionExpiredException;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoVerificationResourceImpl implements SsoVerificationResource {

  @Inject SsoVerificationDatasource verificationDatasource;
  @Inject SsoTfaService ssoTfaService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> complete(String verificationId, TfaCompleteDTO body) {
    log.info("[AUTH] Complete verification={} request", verificationId);
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

  @Override
  public CompletionStage<Response> get(String id) {
    log.info("[AUTH]: Get verification={}, request", id);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return verificationDatasource
        .retrieveUserByVerificationId(id)
        .thenApply(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return DataResponse.error(Boom.notFound())
                    .builder()
                    .cookie(SsoVerification.clearCookie(cookieDomain))
                    .build();
              }
              return DataResponse.ok(true);
            });
  }
}
