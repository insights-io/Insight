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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoResourceImpl implements SsoResource {

  private static final Logger log = LoggerFactory.getLogger(SsoResourceImpl.class);

  @Inject SsoService ssoService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> login(String email, String password) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoService
        .login(email, password)
        .thenApply(
            sessionId ->
                Response.noContent().cookie(SsoSession.cookie(sessionId, cookieDomain)).build());
  }

  @Override
  public CompletionStage<Response> logout(String sessionId) {
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
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return ssoService
        .findSession(sessionId)
        .thenApply(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                log.info("sessionId={} not found", sessionId);
                return Response.noContent().cookie(SsoSession.clearCookie(cookieDomain)).build();
              }
              return DataResponse.ok(maybeUser.get());
            });
  }

  @Override
  public CompletionStage<Response> me(String sessionId) {
    return session(sessionId);
  }
}
