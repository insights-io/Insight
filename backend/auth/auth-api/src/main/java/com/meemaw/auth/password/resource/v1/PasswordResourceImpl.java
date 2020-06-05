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
import javax.ws.rs.core.UriInfo;

public class PasswordResourceImpl implements PasswordResource {

  @Inject PasswordService passwordService;
  @Inject SsoService ssoService;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> forgotPassword(
      PasswordForgotRequestDTO passwordForgotRequestDTO) {
    String clientBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseGet(() -> RequestUtils.getServerBaseURL(info, request));

    return passwordService
        .forgotPassword(passwordForgotRequestDTO.getEmail(), clientBaseURL)
        .thenApply(user -> DataResponse.created(true));
  }

  @Override
  public CompletionStage<Response> resetPassword(UUID token, PasswordResetRequestDTO payload) {
    String password = payload.getPassword();
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return passwordService
        .resetPassword(token, password)
        .thenCompose(
            passwordResetRequest ->
                ssoService
                    .login(passwordResetRequest.getEmail(), password)
                    .thenApply(sessionId -> SsoSession.cookieResponse(sessionId, cookieDomain)));
  }

  @Override
  public CompletionStage<Response> passwordResetRequestExists(UUID token) {
    return passwordService.passwordResetRequestExists(token).thenApply(DataResponse::ok);
  }
}
