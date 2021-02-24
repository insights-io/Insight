package com.rebrowse.auth.accounts.datasource.challenge;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallenge;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallengeType;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AuthorizationChallengeDatasource {

  CompletionStage<String> create(AuthorizationChallenge challenge);

  CompletionStage<Optional<AuthorizationChallenge>> retrieve(
      String challengeId, AuthorizationChallengeType type);

  void delete(String id);
}
