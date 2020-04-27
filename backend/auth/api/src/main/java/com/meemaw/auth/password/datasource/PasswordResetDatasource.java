package com.meemaw.auth.password.datasource;

import com.meemaw.auth.password.model.PasswordResetRequest;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordResetDatasource {

  CompletionStage<Boolean> delete(Transaction transaction, UUID token, String email, String org);

  CompletionStage<Optional<PasswordResetRequest>> find(UUID token, String email, String org);

  CompletionStage<PasswordResetRequest> create(Transaction transaction, String email, UUID userId,
      String org);

  CompletionStage<Boolean> exists(String email, String org, UUID token);
}
