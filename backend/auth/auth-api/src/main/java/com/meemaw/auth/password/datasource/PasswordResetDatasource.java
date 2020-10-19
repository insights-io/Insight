package com.meemaw.auth.password.datasource;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordResetDatasource {

  CompletionStage<Boolean> deletePasswordResetRequest(UUID token, SqlTransaction transaction);

  CompletionStage<Optional<PasswordResetRequest>> findPasswordResetRequest(UUID token);

  CompletionStage<PasswordResetRequest> createPasswordResetRequest(
      String email, UUID userId, String organizationId, SqlTransaction transaction);
}
