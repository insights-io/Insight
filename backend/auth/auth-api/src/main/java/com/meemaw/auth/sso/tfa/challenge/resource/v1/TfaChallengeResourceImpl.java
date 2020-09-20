package com.meemaw.auth.sso.tfa.challenge.resource.v1;

import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.VerificationSessionExpiredException;
import com.meemaw.auth.sso.tfa.challenge.datasource.TfaChallengeDatasource;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.sso.tfa.sms.impl.TfaSmsProvider;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TfaChallengeResourceImpl implements TfaChallengeResource {

  @Inject TfaChallengeService tfaChallengeService;
  @Inject TfaChallengeDatasource tfaChallengeDatasource;
  @Inject UserDatasource userDatasource;
  @Inject TfaSmsProvider tfaSmsProvider;
  @Inject UserTfaDatasource userTfaDatasource;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> complete(
      TfaMethod method, String challengeId, TfaChallengeCompleteDTO body) {
    log.info("[AUTH] {} TFA complete request for challengeId={}", method, challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaChallengeService
        .complete(method, body.getCode(), challengeId)
        .thenApply(
            sessionId ->
                SsoSession.cookieResponseBuilder(sessionId, cookieDomain)
                    .cookie(SsoChallenge.clearCookie(cookieDomain))
                    .build())
        .exceptionally(
            throwable -> {
              if (throwable.getCause() instanceof VerificationSessionExpiredException) {
                return ((VerificationSessionExpiredException) throwable.getCause())
                    .response(cookieDomain);
              }

              throw (RuntimeException) throwable;
            });
  }

  @Override
  public CompletionStage<Response> get(String challengeId) {
    log.info("[AUTH]: Get challengeId={} request", challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaChallengeDatasource
        .retrieveUser(challengeId)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return CompletableFuture.completedStage(challengeNotFoundResponse(cookieDomain));
              }
              return userTfaDatasource.listMethods(maybeUserId.get()).thenApply(DataResponse::ok);
            });
  }

  @Override
  public CompletionStage<Response> sendCode(String challengeId) {
    log.info("[AUTH]: Send TFA SMS code challengeId={} request", challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaChallengeDatasource
        .retrieveUser(challengeId)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                log.debug(
                    "[AUTH]: Could not send TFA SMS code challengeId={} due to missing user",
                    challengeId);
                return CompletableFuture.completedStage(challengeNotFoundResponse(cookieDomain));
              }
              UUID userId = maybeUserId.get();
              return userDatasource
                  .findUser(userId)
                  .thenCompose(
                      maybeUser -> {
                        if (maybeUser.isEmpty()) {
                          log.debug(
                              "[AUTH]: Could not send TFA SMS code challengeId={} due to missing user={}",
                              userId,
                              challengeId);
                          return CompletableFuture.completedStage(
                              challengeNotFoundResponse(cookieDomain));
                        }

                        AuthUser user = maybeUser.get();
                        if (!user.isPhoneNumberVerified()) {
                          throw Boom.badRequest()
                              .errors(Errors.PHONE_NUMBER_VERIFICATION_REQUIRED)
                              .exception();
                        }

                        return tfaSmsProvider
                            .prepareChallenge(userId, user.getPhoneNumber())
                            .thenApply(DataResponse::ok);
                      });
            });
  }

  private Response challengeNotFoundResponse(String cookieDomain) {
    return DataResponse.error(Boom.notFound())
        .builder()
        .cookie(SsoChallenge.clearCookie(cookieDomain))
        .build();
  }
}
