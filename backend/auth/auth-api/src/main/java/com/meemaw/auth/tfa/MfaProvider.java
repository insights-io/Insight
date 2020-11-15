package com.meemaw.auth.tfa;

import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.user.model.AuthUser;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface MfaProvider<T> {

  CompletionStage<Boolean> completeChallenge(
      String challengeId, int code, MfaConfiguration mfaConfiguration)
      throws MfaChallengeValidatationException;

  CompletionStage<Pair<MfaConfiguration, AuthUser>> setupComplete(AuthUser user, int code);

  CompletionStage<T> setupStart(AuthUser user, boolean isChallenged);

  MfaMethod getMethod();
}
