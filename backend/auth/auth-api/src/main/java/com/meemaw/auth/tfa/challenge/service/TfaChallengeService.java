package com.meemaw.auth.tfa.challenge.service;

import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.tfa.TfaChallengeValidatationException;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.TfaProvider;
import com.meemaw.auth.tfa.TfaProvidersRegistry;
import com.meemaw.auth.tfa.VerificationSessionExpiredException;
import com.meemaw.auth.tfa.challenge.datasource.TfaChallengeDatasource;
import com.meemaw.auth.tfa.model.TfaSetup;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class TfaChallengeService {

  public static final Map<String, String> INVALID_CODE_ERRORS = Map.of("code", "Invalid code");

  @Inject TfaChallengeDatasource tfaChallengeDatasource;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject TfaProvidersRegistry tfaProvidersRegistry;
  @Inject SsoService ssoService;

  public CompletionStage<String> start(UUID userId) {
    return tfaChallengeDatasource.create(userId);
  }

  public CompletionStage<String> complete(TfaMethod method, int code, String challengeId) {
    return tfaChallengeDatasource
        .retrieve(challengeId)
        .thenCompose(
            maybeUserId -> {
              UUID userId =
                  maybeUserId.orElseThrow(
                      () -> {
                        log.info("[AUTH]: Challenge session {} expired", challengeId);
                        throw new VerificationSessionExpiredException();
                      });

              return userTfaDatasource
                  .get(userId, method)
                  .thenCompose(
                      maybeTfaSetup -> {
                        TfaSetup tfaSetup =
                            maybeTfaSetup.orElseThrow(
                                () -> {
                                  log.info(
                                      "[AUTH]: {} TFA complete attempt for user={} which is not configured",
                                      method,
                                      userId);
                                  throw new VerificationSessionExpiredException(
                                      String.format("%s TFA not configured", method));
                                });

                        TfaProvider<?> tfaProvider = tfaProvidersRegistry.get(method);
                        try {
                          return tfaProvider
                              .completeChallenge(challengeId, code, tfaSetup)
                              .thenCompose(
                                  isValid -> {
                                    if (!isValid) {
                                      log.info(
                                          "[AUTH]: Invalid code for {} TFA challenge for user={}",
                                          method,
                                          userId);
                                      throw Boom.badRequest()
                                          .errors(INVALID_CODE_ERRORS)
                                          .exception();
                                    }

                                    tfaChallengeDatasource.delete(challengeId);
                                    return ssoService.createSession(userId);
                                  });
                        } catch (TfaChallengeValidatationException ex) {
                          log.error(
                              "[AUTH]: Something went wrong while validating TFA code for user={}",
                              userId,
                              ex);
                          throw Boom.serverError().exception(ex);
                        }
                      });
            });
  }
}
