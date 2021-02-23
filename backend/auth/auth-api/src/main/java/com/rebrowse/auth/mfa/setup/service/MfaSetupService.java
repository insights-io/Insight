package com.rebrowse.auth.mfa.setup.service;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.MfaProvidersRegistry;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
@Slf4j
public class MfaSetupService {

  @Inject UserMfaDatasource userMfaDatasource;
  @Inject MfaProvidersRegistry mfaProvidersRegistry;

  public CompletionStage<Boolean> mfaSetupDisable(UUID userId, MfaMethod method) {
    return userMfaDatasource
        .delete(userId, method)
        .thenApply(
            deleted -> {
              if (!deleted) {
                throw Boom.notFound().exception();
              }
              return true;
            });
  }

  public CompletionStage<?> startSetup(String sessionId, MfaMethod method, AuthUser user) {
    if (method.equals(MfaMethod.SMS) && !user.isPhoneNumberVerified()) {
      throw Boom.badRequest()
          .errors(UserTable.Errors.PHONE_NUMBER_VERIFICATION_REQUIRED)
          .exception();
    }
    return startChallengeSetup(sessionId, method, user);
  }

  public CompletionStage<?> startChallengeSetup(
      String challengeId, MfaMethod method, AuthUser user) {
    return mfaProvidersRegistry.get(method).startSetup(challengeId, user);
  }

  public CompletionStage<Pair<MfaConfiguration, AuthUser>> completeSetup(
      String sessionId, MfaMethod method, AuthUser user, int code) {
    return mfaProvidersRegistry.get(method).completeSetup(sessionId, user, code);
  }
}
