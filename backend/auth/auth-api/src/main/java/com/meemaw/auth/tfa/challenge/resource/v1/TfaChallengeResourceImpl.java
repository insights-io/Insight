package com.meemaw.auth.tfa.challenge.resource.v1;

import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;

import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.VerificationSessionExpiredException;
import com.meemaw.auth.tfa.challenge.datasource.TfaChallengeDatasource;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.sms.impl.TfaSmsProvider;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
  public CompletionStage<Response> list(String id) {
    log.info("[AUTH]: Get challengeId={} request", id);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaChallengeDatasource
        .retrieveUserByChallengeId(id)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return CompletableFuture.completedStage(challengeNotFoundResponse(cookieDomain));
              }
              return userTfaDatasource.listMethods(maybeUserId.get()).thenApply(DataResponse::ok);
            });
  }

  @Override
  public CompletionStage<Response> sendSmsChallengeCode(String challengeId) {
    log.info("[AUTH]: Send TFA SMS code challengeId={} request", challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return tfaChallengeDatasource
        .retrieveUserByChallengeId(challengeId)
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

                        String codeKey = TfaSmsProvider.challengeCodeKey(challengeId);
                        return tfaSmsProvider
                            .sendVerificationCode(codeKey, user.getPhoneNumber())
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
