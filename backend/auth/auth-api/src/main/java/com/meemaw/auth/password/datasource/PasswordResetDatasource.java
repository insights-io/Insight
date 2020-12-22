package com.meemaw.auth.password.datasource;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordResetDatasource {

  CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction);

  CompletionStage<Optional<PasswordResetRequest>> retrieve(UUID token);

  CompletionStage<PasswordResetRequest> create(
      String email, UUID userId, SqlTransaction transaction);
}
