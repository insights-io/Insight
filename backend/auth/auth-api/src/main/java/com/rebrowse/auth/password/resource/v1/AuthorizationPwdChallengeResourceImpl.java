package com.rebrowse.auth.password.resource.v1;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.password.service.PwdAuthorizationChallengeService;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class AuthorizationPwdChallengeResourceImpl implements AuthorizationPwdChallengeResource {

  @Inject PwdAuthorizationChallengeService pwdAuthorizationChallengeService;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> retrieve(String challengeId) {
    return pwdAuthorizationChallengeService
        .retrieveChallenge(challengeId)
        .thenApply(maybeResponse -> maybeResponse.orElseThrow(() -> Boom.notFound().exception()))
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> complete(String challengeId, String email, String password) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    String domain = URIUtils.parseCookieDomain(serverBaseUri);
    return pwdAuthorizationChallengeService
        .completeChallenge(challengeId, email, password, serverBaseUri)
        .thenApply(auth -> auth.response(AuthorizationPwdChallengeSession.clearCookie(domain)));
  }
}
