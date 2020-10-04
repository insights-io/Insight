package com.meemaw.auth.tfa.challenge.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaChallengeDatasource {

  CompletionStage<String> createChallengeForUser(UUID userId);

  CompletionStage<Optional<UUID>> retrieveUserByChallengeId(String challengeId);

  void deleteChallenge(String challengeId);
}
