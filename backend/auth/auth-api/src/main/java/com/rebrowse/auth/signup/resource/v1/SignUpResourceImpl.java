package com.rebrowse.auth.signup.resource.v1;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.signup.model.dto.SignUpRequestDTO;
import com.rebrowse.auth.signup.service.SignUpServiceImpl;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUpResourceImpl implements SignUpResource {

  @Inject SignUpServiceImpl signUpService;
  @Inject SsoService ssoService;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> create(SignUpRequestDTO payload) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return signUpService
        .signUp(serverBaseUri, payload)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> checkIfValid(UUID token) {
    return signUpService.signUpRequestValid(token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> complete(UUID token) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return signUpService
        .completeSignUp(token)
        .thenCompose(
            userSignUpRequestPair -> {
              AuthUser user = userSignUpRequestPair.getLeft();
              URI redirect = RequestUtils.sneakyUri(userSignUpRequestPair.getRight().getRedirect());
              String email = user.getEmail();
              AuthorizationRequest authorizationRequest =
                  new AuthorizationRequest(email, redirect, serverBaseUri);

              return ssoService
                  .authorizeSignUpSuccess(user, authorizationRequest)
                  .thenApply(AuthorizationResponse::response);
            });
  }
}
