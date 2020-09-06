package com.meemaw.auth.sso.tfa.totp.datasource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TfaTotpSetupDatasource {

  CompletionStage<Optional<String>> getTotpSecret(UUID userId);

  CompletionStage<Void> setTotpSecret(UUID userId, String secret);

  void deleteTotpSecret(UUID userId);
}
