package com.meemaw.auth.password.datasource;

import com.meemaw.auth.password.model.PasswordResetRequest;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordResetDatasource {

  /**
   * Delete password reset request by confirmation token.
   *
   * @param token UUID confirmation token
   * @param transaction Transaction context
   * @return Boolean indicating successful deletion
   */
  CompletionStage<Boolean> deletePasswordResetRequest(UUID token, Transaction transaction);

  /**
   * Find password reset request by confirmation token.
   *
   * @param token UUID confirmation token
   * @return maybe PasswordResetRequest
   */
  CompletionStage<Optional<PasswordResetRequest>> findPasswordResetRequest(UUID token);

  /**
   * Create password reset request.
   *
   * @param email String email address
   * @param userId UUID user id
   * @param transaction Transaction context
   * @return newly created PasswordResetRequest
   */
  CompletionStage<PasswordResetRequest> createPasswordResetRequest(
      String email, UUID userId, Transaction transaction);
}
