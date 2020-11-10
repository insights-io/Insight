package com.meemaw.auth.tfa.challenge.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaChallengeDatasource {

  CompletionStage<String> create(UUID userId);

  CompletionStage<Optional<UUID>> retrieve(String challengeId);

  void delete(String challengeId);
}
