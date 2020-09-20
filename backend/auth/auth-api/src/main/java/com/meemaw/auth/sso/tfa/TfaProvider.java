package com.meemaw.auth.sso.tfa;

import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaProvider<T> {

  CompletionStage<Boolean> validate(int code, TfaSetup tfaSetup)
      throws TfaChallengeValidatationException;

  CompletionStage<TfaSetup> setupComplete(UUID userId, int code);

  CompletionStage<T> setupStart(UUID userId, String email);

  TfaMethod getMethod();
}