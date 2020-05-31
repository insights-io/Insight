package com.meemaw.auth.password.resource.v1;

import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

public class PasswordResourceImpl implements PasswordResource {

  @Inject PasswordService passwordService;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> forgot(PasswordForgotRequestDTO passwordForgotRequestDTO) {
    return passwordService
        .forgot(passwordForgotRequestDTO.getEmail())
        .thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> reset(PasswordResetRequestDTO payload) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return passwordService
        .reset(payload)
        .thenCompose(
            x -> {
              String email = payload.getEmail();
              String password = payload.getPassword();
              return ssoService
                  .login(email, password)
                  .thenApply(
                      sessionId ->
                          Response.noContent()
                              .cookie(SsoSession.cookie(sessionId, cookieDomain))
                              .build());
            });
  }

  @Override
  public CompletionStage<Response> resetRequestExists(String email, String org, UUID token) {
    return passwordService.resetRequestExists(email, org, token).thenApply(DataResponse::ok);
  }
}
