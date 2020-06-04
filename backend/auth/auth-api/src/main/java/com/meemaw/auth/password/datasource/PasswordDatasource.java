package com.meemaw.auth.password.datasource;

import com.meemaw.auth.user.model.UserWithHashedPassword;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordDatasource {

  /**
   * Store user password.
   *
   * @param userId UUID user id
   * @param hashedPassword String hashed password
   * @param transaction Transaction context
   * @return Boolean indicating successful insert
   */
  CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, Transaction transaction);

  /**
   * Find user with its password.
   *
   * @param email String email address
   * @return maybe UserWithHashedPassword
   */
  CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email);
}
