package com.meemaw.auth.user.datasource;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface UserDatasource {

  /**
   * Create a new user.
   *
   * @param email String email address
   * @param fullName String full name
   * @param organizationId String organization id
   * @param role UserRole user role
   * @param transaction Transaction context
   * @return newly created AuthUser
   */
  CompletionStage<AuthUser> createUser(
      String email, String fullName, String organizationId, UserRole role, Transaction transaction);

  /**
   * Find user by email address.
   *
   * @param email String email address.
   * @return maybe AuthUser
   */
  CompletionStage<Optional<AuthUser>> findUser(String email);

  /**
   * Find user by email address.
   *
   * @param email String email address
   * @param transaction Transaction context
   * @return maybe AuthUser
   */
  CompletionStage<Optional<AuthUser>> findUser(String email, Transaction transaction);
}
