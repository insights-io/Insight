package com.meemaw.auth.sso.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SsoVerificationDatasource {

  CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId);

  CompletionStage<Void> setTfaSetupSecret(UUID userId, String secret);

  CompletionStage<String> deleteTfaSetupSecret(UUID userId);

  CompletionStage<String> createVerificationId(UUID userId);

  CompletionStage<Optional<UUID>> retrieveUserByVerificationId(String verificationId);

  CompletionStage<Optional<UUID>> deleteVerificationId(String verificationId);
}
