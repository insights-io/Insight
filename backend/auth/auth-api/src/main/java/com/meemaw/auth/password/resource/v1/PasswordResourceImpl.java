package com.meemaw.auth.password.resource.v1;

import com.meemaw.auth.password.model.dto.PasswordChangeRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.password.service.PasswordService;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordResourceImpl implements PasswordResource {

  @Inject PasswordService passwordService;
  @Inject SsoService ssoService;
  @Inject AuthPrincipal authPrincipal;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> forgot(PasswordForgotRequestDTO passwordForgotRequestDTO) {
    URL clientBaseURL =
        RequestUtils.parseReferrerOrigin(request)
            .orElseGet(() -> RequestUtils.getServerBaseUrl(info, request));

    return passwordService
        .forgotPassword(passwordForgotRequestDTO.getEmail(), clientBaseURL)
        .thenApply(maybeUser -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> reset(UUID token, PasswordResetRequestDTO payload) {
    String password = payload.getPassword();
    URL serverBaseURL = RequestUtils.getServerBaseUrl(info, request);
    String cookieDomain = RequestUtils.parseCookieDomain(serverBaseURL);
    String ipAddress = RequestUtils.getRemoteAddress(request);

    return passwordService
        .resetPassword(token, password)
        .thenCompose(
            passwordResetRequest ->
                ssoService
                    .passwordLogin(passwordResetRequest.getEmail(), password, ipAddress, null, null)
                    .thenApply(loginResult -> loginResult.loginResponse(cookieDomain)));
  }

  @Override
  public CompletionStage<Response> resetRequestExists(UUID token) {
    return passwordService.passwordResetRequestExists(token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> change(PasswordChangeRequestDTO body) {
    body.validate();
    AuthUser user = authPrincipal.user();
    return passwordService
        .changePassword(
            user.getId(),
            user.getEmail(),
            user.getOrganizationId(),
            body.getCurrentPassword(),
            body.getNewPassword())
        .thenApply(ignored -> DataResponse.noContent());
  }
}
