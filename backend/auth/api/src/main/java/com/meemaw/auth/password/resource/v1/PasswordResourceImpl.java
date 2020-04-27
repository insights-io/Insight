package com.meemaw.auth.password.resource.v1;

import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.auth.SsoSession;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class PasswordResourceImpl implements PasswordResource {

  @Inject
  PasswordService passwordService;

  @Inject
  SsoService ssoService;

  @Override
  public CompletionStage<Response> forgot(PasswordForgotRequestDTO passwordForgotRequestDTO) {
    return passwordService.forgot(passwordForgotRequestDTO.getEmail())
        .thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> reset(PasswordResetRequestDTO passwordResetRequestDTO) {
    return passwordService.reset(passwordResetRequestDTO).thenCompose(x -> {
      String email = passwordResetRequestDTO.getEmail();
      String password = passwordResetRequestDTO.getPassword();
      return ssoService.login(email, password)
          .thenApply(
              sessionId -> Response.noContent().cookie(SsoSession.cookie(sessionId)).build());
    });
  }

  @Override
  public CompletionStage<Response> resetRequestExists(String email, String org, UUID token) {
    return passwordService.resetRequestExists(email, org, token).thenApply(DataResponse::ok);
  }
}
