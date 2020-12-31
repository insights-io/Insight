package com.meemaw.auth.mfa.challenge.resource.v1;

import com.meemaw.auth.mfa.ChallengeSessionExpiredException;
import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.challenge.datasource.MfaChallengeDatasource;
import com.meemaw.auth.mfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.meemaw.auth.mfa.sms.impl.MfaSmsProvider;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.datasource.UserTable.Errors;
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
public class MfaChallengeResourceImpl implements MfaChallengeResource {

  @Inject MfaChallengeService mfaChallengeService;
  @Inject MfaChallengeDatasource mfaChallengeDatasource;
  @Inject UserDatasource userDatasource;
  @Inject MfaSmsProvider smsProvider;
  @Inject UserMfaDatasource userMfaDatasource;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> complete(
      MfaMethod method, String challengeId, MfaChallengeCompleteDTO body) {
    log.info("[AUTH] {} MFA complete request for challengeId={}", method, challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());

    return mfaChallengeService
        .complete(method, body.getCode(), challengeId)
        .thenApply(
            sessionId ->
                SsoSession.cookieResponseBuilder(sessionId, cookieDomain)
                    .cookie(SsoChallenge.clearCookie(cookieDomain))
                    .build())
        .exceptionally(
            throwable -> {
              if (throwable.getCause() instanceof ChallengeSessionExpiredException) {
                return ((ChallengeSessionExpiredException) throwable.getCause())
                    .response(cookieDomain);
              }

              throw (RuntimeException) throwable;
            });
  }

  @Override
  public CompletionStage<Response> list(String id) {
    log.info("[AUTH]: Get challengeId={} request", id);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return mfaChallengeDatasource
        .retrieve(id)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return CompletableFuture.completedStage(challengeNotFoundResponse(cookieDomain));
              }
              return userMfaDatasource.listMethods(maybeUserId.get()).thenApply(DataResponse::ok);
            });
  }

  @Override
  public CompletionStage<Response> sendSmsChallengeCode(String challengeId) {
    log.info("[AUTH]: Send MFA SMS code challengeId={} request", challengeId);
    String cookieDomain = RequestUtils.parseCookieDomain(request.absoluteURI());
    return mfaChallengeDatasource
        .retrieve(challengeId)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                log.debug(
                    "[AUTH]: Could not send MFA SMS code challengeId={} due to missing user",
                    challengeId);
                return CompletableFuture.completedStage(challengeNotFoundResponse(cookieDomain));
              }
              UUID userId = maybeUserId.get();
              return userDatasource
                  .retrieve(userId)
                  .thenCompose(
                      maybeUser -> {
                        if (maybeUser.isEmpty()) {
                          log.debug(
                              "[AUTH]: Could not send MFA SMS code challengeId={} due to missing user={}",
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

                        String codeKey = MfaSmsProvider.challengeCodeKey(challengeId);
                        return smsProvider
                            .sendVerificationCode(codeKey, user.getPhoneNumber())
                            .thenApply(DataResponse::ok);
                      });
            });
  }

  @Override
  public CompletionStage<Response> retrieveUser(String challengeId) {
    return mfaChallengeService
        .retrieveUser(challengeId)
        .thenApply(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeUser.get());
            });
  }

  private Response challengeNotFoundResponse(String cookieDomain) {
    return DataResponse.error(Boom.notFound())
        .builder()
        .cookie(SsoChallenge.clearCookie(cookieDomain))
        .build();
  }
}
