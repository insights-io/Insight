package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.SignUpRequest;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SignUpDatasource {

  /**
   * Create a sign up request.
   *
   * @param signUpRequest sign up request
   * @param transaction transaction context
   * @return UUID confirmation token
   */
  CompletionStage<UUID> createSignUpRequest(SignUpRequest signUpRequest, Transaction transaction);

  /**
   * Finds SignUpRequest associated with the token.
   *
   * @param token sign up request token
   * @return SignUpRequest if present
   */
  CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token);

  /**
   * Finds SignUpRequest associated with the token.
   *
   * @param token sign up request token
   * @param transaction context
   * @return SignUpRequest if present
   */
  CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token, Transaction transaction);

  /**
   * Delete SignUpRequest associated with the token exists.
   *
   * @param token sign up request token
   * @param transaction context
   * @return boolean indicating if token has been deleted
   */
  CompletionStage<Boolean> deleteSignUpRequest(UUID token, Transaction transaction);

  /**
   * Check if email address is taken.
   *
   * @param email String email address
   * @param transaction Transaction context
   * @return Boolean indicating if email address is taken
   */
  CompletionStage<Boolean> selectIsEmailTaken(String email, Transaction transaction);
}
