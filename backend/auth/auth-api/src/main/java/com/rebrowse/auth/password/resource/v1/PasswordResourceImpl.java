package com.rebrowse.auth.password.resource.v1;

import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.password.model.dto.PasswordChangeRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordForgotRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordResetRequestDTO;
import com.rebrowse.auth.password.service.PasswordService;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.model.AuthUser;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
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

  @Inject AuthPrincipal authPrincipal;
  @Inject PasswordService passwordService;

  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> forgot(PasswordForgotRequestDTO payload) {
    String email = payload.getEmail();
    URL redirect = payload.getRedirect();
    URI resetLocation =
        RequestUtils.parseReferrerOrigin(request)
            .orElseGet(() -> RequestUtils.getServerBaseUri(info, request));

    return passwordService
        .forgotPassword(email, redirect, resetLocation)
        .thenApply(maybeUser -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> reset(UUID token, PasswordResetRequestDTO payload) {
    String password = payload.getPassword();
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return passwordService
        .reset(token, password, serverBaseUri)
        .thenApply(AuthorizationResponse::response);
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
