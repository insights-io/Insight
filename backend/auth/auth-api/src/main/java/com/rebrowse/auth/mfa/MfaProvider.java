package com.rebrowse.auth.mfa;

import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.user.model.AuthUser;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface MfaProvider<T> {

  MfaMethod getMethod();

  CompletionStage<Boolean> completeChallenge(
      String challengeId, int code, MfaConfiguration mfaConfiguration)
      throws MfaChallengeValidatationException;

  CompletionStage<Pair<MfaConfiguration, AuthUser>> completeSetup(
      String sessionId, AuthUser user, int code);

  CompletionStage<T> startSetup(String identifier, AuthUser user);
}
