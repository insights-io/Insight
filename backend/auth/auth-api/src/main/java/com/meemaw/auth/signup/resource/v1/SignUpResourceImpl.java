package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.service.SignUpServiceImpl;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUpResourceImpl implements SignUpResource {

  @Inject SignUpServiceImpl signUpService;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> signUp(SignUpRequestDTO payload) {
    String referer = RequestUtils.parseRefererBaseURL(request).orElse(null);
    String serverBaseURL = RequestUtils.getServerBaseURL(info, request);

    log.info("REFERER: {}", referer);

    return signUpService
        .signUp(referer, serverBaseURL, payload)
        .thenApply(ignored -> Response.noContent().build());
  }

  @Override
  public CompletionStage<Response> signUpRequestValid(UUID token) {
    return signUpService.signUpRequestValid(token).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> signUpRequestComplete(UUID token) {
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return signUpService
        .completeSignUp(token)
        .thenCompose(
            userAndSignUpRequest ->
                ssoService
                    .createSession(userAndSignUpRequest.getLeft())
                    .thenApply(
                        sessionId ->
                            buildSignUpCompleteResponse(
                                sessionId, userAndSignUpRequest.getRight(), cookieDomain)));
  }

  private Response buildSignUpCompleteResponse(
      String sessionId, SignUpRequest signUpRequest, String cookieDomain) {
    Optional<String> maybeRefererCallbackURL = signUpRequest.getRefererCallbackURL();
    if (maybeRefererCallbackURL.isEmpty()) {
      return SsoSession.cookieResponse(sessionId, cookieDomain);
    }
    return Response.status(Status.FOUND)
        .header("Location", maybeRefererCallbackURL.get())
        .cookie(SsoSession.cookie(sessionId, cookieDomain))
        .build();
  }
}
