package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import com.meemaw.auth.signup.service.SignupService;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.auth.SsoSession;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class SignupResourceImpl implements SignupResource {

  @Inject
  SignupService signupService;

  @Inject
  SsoService ssoService;

  @Override
  public CompletionStage<Response> signup(String email) {
    return signupService.create(email).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> signupExists(String email, String org, UUID token) {
    return signupService.exists(email, org, token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> signupComplete(SignupRequestCompleteDTO req) {
    return signupService.complete(req).thenCompose(x -> {
      String email = req.getEmail();
      String password = req.getPassword();
      return ssoService.login(email, password)
          .thenApply(
              sessionId -> Response.noContent().cookie(SsoSession.cookie(sessionId)).build());
    });
  }
}
