package com.meemaw.auth.verification.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaSetupDatasource {

  CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId);

  CompletionStage<Void> storeTfaSetupSecret(UUID userId, String secret);

  CompletionStage<String> removeTfaSetupSecret(UUID userId);

  CompletionStage<String> createTfaClientId(UUID userId);

  CompletionStage<Optional<UUID>> retrieveUserIdFromTfaClientId(String tfaClientId);
}
