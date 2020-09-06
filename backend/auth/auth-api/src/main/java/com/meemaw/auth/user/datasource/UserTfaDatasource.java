package com.meemaw.auth.user.datasource;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public interface UserTfaDatasource {

  CompletionStage<List<TfaSetup>> list(UUID userId);

  CompletionStage<Optional<TfaSetup>> get(UUID userId, TfaMethod tfaMethod);

  CompletionStage<Boolean> delete(UUID userId, TfaMethod method);

  CompletionStage<TfaSetup> store(
      UUID userId, TfaMethod method, JsonObject params, SqlTransaction sqlTransaction);

  default CompletionStage<TfaSetup> storeTotpTfa(
      UUID userId, String secret, SqlTransaction transaction) {
    JsonObject params = new JsonObject();
    params.put("secret", secret);
    return store(userId, TfaMethod.TOTP, params, transaction);
  }

  default CompletionStage<TfaSetup> storeSmsTfa(UUID userId, SqlTransaction transaction) {
    return store(userId, TfaMethod.SMS, new JsonObject(), transaction);
  }

  default CompletionStage<List<TfaMethod>> listMethods(UUID userId) {
    return list(userId)
        .thenApply(setups -> setups.stream().map(TfaSetup::getMethod).collect(Collectors.toList()));
  }
}
