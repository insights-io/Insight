package com.meemaw.auth.tfa.totp.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaTotpSetupDatasource {

  CompletionStage<Optional<String>> retrieve(UUID userId);

  CompletionStage<Void> set(UUID userId, String secret);

  void delete(UUID userId);
}
