package com.meemaw.auth.user.datasource;

import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public interface UserMfaDatasource {

  CompletionStage<List<MfaConfiguration>> list(UUID userId);

  CompletionStage<Optional<MfaConfiguration>> get(UUID userId, MfaMethod mfaMethod);

  CompletionStage<Boolean> delete(UUID userId, MfaMethod method);

  CompletionStage<MfaConfiguration> store(
      UUID userId, MfaMethod method, JsonObject params, SqlTransaction sqlTransaction);

  default CompletionStage<MfaConfiguration> storeTotpTfa(
      UUID userId, String secret, SqlTransaction transaction) {
    JsonObject params = new JsonObject();
    params.put("secret", secret);
    return store(userId, MfaMethod.TOTP, params, transaction);
  }

  default CompletionStage<MfaConfiguration> storeSmsTfa(UUID userId, SqlTransaction transaction) {
    return store(userId, MfaMethod.SMS, new JsonObject(), transaction);
  }

  default CompletionStage<List<MfaMethod>> listMethods(UUID userId) {
    return list(userId)
        .thenApply(
            setups ->
                setups.stream().map(MfaConfiguration::getMethod).collect(Collectors.toList()));
  }
}
