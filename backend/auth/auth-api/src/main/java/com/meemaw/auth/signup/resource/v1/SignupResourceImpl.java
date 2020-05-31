package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import com.meemaw.auth.signup.service.SignupService;
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

public class SignupResourceImpl implements SignupResource {

  @Inject SignupService signupService;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> signup(String email) {
    return signupService.create(email).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> signupExists(String email, String org, UUID token) {
    return signupService.exists(email, org, token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> signupComplete(SignupRequestCompleteDTO payload) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return signupService
        .complete(payload)
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
}
