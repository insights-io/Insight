package com.rebrowse.auth.mfa.challenge.resource.v1;

import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.setup.service.MfaAuthorizationChallengeService;
import com.rebrowse.auth.mfa.setup.service.MfaSetupService;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class AuthorizationMfaChallengeResourceImpl implements AuthorizationMfaChallengeResource {

  @Inject AuthPrincipal principal;
  @Inject MfaAuthorizationChallengeService mfaAuthorizationChallengeService;
  @Inject MfaSetupService mfaSetupService;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> retrieve(String challengeId) {
    return mfaAuthorizationChallengeService
        .retrieveChallenge(challengeId)
        .thenApply(maybeResponse -> maybeResponse.orElseThrow(() -> Boom.notFound().exception()))
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> setup(String challengeId, MfaMethod method) {
    AuthUser user = principal.user();
    return mfaSetupService
        .startChallengeSetup(challengeId, method, user)
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> complete(
      String challengeId, MfaMethod method, MfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return mfaAuthorizationChallengeService
        .completeChallenge(user, challengeId, method, body.getCode(), serverBaseUri)
        .thenApply(AuthorizationResponse::response);
  }

  @Override
  public CompletionStage<Response> completeEnforced(
      String challengeId, MfaMethod method, MfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    return mfaAuthorizationChallengeService
        .completeEnforcedChallenge(user, challengeId, method, body.getCode(), serverBaseUri)
        .thenApply(AuthorizationResponse::response);
  }

  @Override
  public CompletionStage<Response> sendSmsCode(String challengeId) {
    AuthUser user = principal.user();
    return mfaAuthorizationChallengeService
        .sendSmsCode(user, challengeId)
        .thenApply(DataResponse::ok);
  }
}
