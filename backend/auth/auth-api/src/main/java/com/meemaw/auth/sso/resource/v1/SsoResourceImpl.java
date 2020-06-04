package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class SsoResourceImpl implements SsoResource {

  @Inject SsoService ssoService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> login(String email, String password) {
    MDC.put("email", email);
    log.debug("Login request");
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoService
        .login(email, password)
        .thenApply(
            sessionId ->
                Response.noContent().cookie(SsoSession.cookie(sessionId, cookieDomain)).build());
  }

  @Override
  public CompletionStage<Response> logout(String sessionId) {
    MDC.put(SsoSession.COOKIE_NAME, sessionId);
    log.info("Logout request");
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoService
        .logout(sessionId)
        .thenApply(
            deleted -> {
              ResponseBuilder builder =
                  deleted
                      ? Response.noContent()
                      : DataResponse.error(Boom.badRequest().message("Session does not exist"))
                          .builder();
              return builder.cookie(SsoSession.clearCookie(cookieDomain)).build();
            });
  }

  @Override
  public CompletionStage<Response> session(String sessionId) {
    MDC.put(SsoSession.COOKIE_NAME, sessionId);
    log.debug("Session lookup request");
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoService
        .findSession(sessionId)
        .thenApply(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                log.debug("Session not found");
                return Response.noContent().cookie(SsoSession.clearCookie(cookieDomain)).build();
              }
              return DataResponse.ok(maybeUser.get());
            });
  }
}
