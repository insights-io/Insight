package com.meemaw.auth.password.service;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.user.model.AuthUser;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.mindrot.jbcrypt.BCrypt;

public interface PasswordService {

  /**
   * Verify password associated with email. If matching, return user.
   *
   * @param email String user email address
   * @param password user's password
   * @return user associated with the provided credentials
   */
  CompletionStage<AuthUser> verifyPassword(String email, String password);

  /**
   * Start forgot password flow.
   *
   * @param email String user email address
   * @param passwordResetBaseURL String password reset base URL address
   * @return maybe AuthUser
   */
  CompletionStage<Optional<AuthUser>> forgotPassword(String email, String passwordResetBaseURL);

  /**
   * Reset password.
   *
   * @param token UUID confirmation token
   * @param password String new password
   * @return PasswordResetRequest password reset request that was executed
   */
  CompletionStage<PasswordResetRequest> resetPassword(UUID token, String password);

  /**
   * Create password associated with user.
   *
   * @param userId UUID user id
   * @param email String user email address
   * @param password String user password
   * @param transaction Transaction transaction context
   * @return Boolean indicating if user was successfully created
   */
  CompletionStage<Boolean> createPassword(
      UUID userId, String email, String password, Transaction transaction);

  /**
   * Check if password reset request exists.
   *
   * @param token UUID password reset request token
   * @return Boolean indicating if password reset request exists
   */
  CompletionStage<Boolean> passwordResetRequestExists(UUID token);

  /**
   * Securely hash password.
   *
   * @param password String password
   * @return String hashed password
   */
  default String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(13));
  }
}
