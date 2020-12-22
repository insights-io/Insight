package com.meemaw.auth.user.datasource;

import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.model.MfaConfiguration;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public interface UserMfaDatasource {

  CompletionStage<Optional<MfaConfiguration>> retrieve(UUID userId, MfaMethod mfaMethod);

  CompletionStage<Collection<MfaConfiguration>> list(UUID userId);

  CompletionStage<Boolean> delete(UUID userId, MfaMethod method);

  CompletionStage<MfaConfiguration> create(
      UUID userId, MfaMethod method, JsonObject params, SqlTransaction sqlTransaction);

  default CompletionStage<MfaConfiguration> createTotpConfiguration(
      UUID userId, String secret, SqlTransaction transaction) {
    JsonObject params = new JsonObject();
    params.put("secret", secret);
    return create(userId, MfaMethod.TOTP, params, transaction);
  }

  default CompletionStage<MfaConfiguration> createSmsConfiguration(
      UUID userId, SqlTransaction transaction) {
    return create(userId, MfaMethod.SMS, new JsonObject(), transaction);
  }

  default CompletionStage<List<MfaMethod>> listMethods(UUID userId) {
    return list(userId)
        .thenApply(
            setups ->
                setups.stream().map(MfaConfiguration::getMethod).collect(Collectors.toList()));
  }
}
