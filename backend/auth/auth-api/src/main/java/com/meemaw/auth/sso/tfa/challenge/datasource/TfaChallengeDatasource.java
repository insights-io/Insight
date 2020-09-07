package com.meemaw.auth.sso.tfa.challenge.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaChallengeDatasource {

  CompletionStage<String> createChallenge(UUID userId);

  CompletionStage<Optional<UUID>> retrieveUser(String challengeId);

  void deleteVerification(String challengeId);
}
