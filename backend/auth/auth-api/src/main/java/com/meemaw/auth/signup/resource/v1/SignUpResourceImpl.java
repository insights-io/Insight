package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.service.SignUpServiceImpl;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
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
public class SignUpResourceImpl implements SignUpResource {

  @Inject SignUpServiceImpl signUpService;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> create(SignUpRequestDTO payload) {
    URL referrer = RequestUtils.parseReferrerOrigin(request).orElse(null);
    URL serverBaseURL = RequestUtils.getServerBaseUrl(info, request);

    return signUpService
        .signUp(referrer, serverBaseURL, payload)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> checkIfValid(UUID token) {
    return signUpService.signUpRequestValid(token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> complete(UUID token) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return signUpService
        .completeSignUp(token)
        .thenCompose(
            userAndSignUpRequest ->
                ssoService
                    .authenticateDirect(
                        userAndSignUpRequest.getLeft(),
                        userAndSignUpRequest
                            .getRight()
                            .getReferrerCallbackUrl()
                            .map(URI::create)
                            .orElse(null))
                    .thenApply(loginResult -> loginResult.loginResponse(cookieDomain)));
  }
}
