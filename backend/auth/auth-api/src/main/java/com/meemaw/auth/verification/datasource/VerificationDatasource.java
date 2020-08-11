package com.meemaw.auth.verification.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface VerificationDatasource {

  CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId);

  CompletionStage<Void> storeTfaSetupSecret(UUID userId, String secret);
}
