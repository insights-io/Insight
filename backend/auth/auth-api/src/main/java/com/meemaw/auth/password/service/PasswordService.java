package com.meemaw.auth.password.service;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.mindrot.jbcrypt.BCrypt;

public interface PasswordService {

  CompletionStage<AuthUser> verifyPassword(String email, String password);

  CompletionStage<Optional<AuthUser>> forgotPassword(String email, String passwordResetBaseURL);

  CompletionStage<PasswordResetRequest> resetPassword(UUID token, String password);

  CompletionStage<Boolean> changePassword(
      UUID userId,
      String email,
      String currentPassword,
      String newPassword,
      String confirmNewPassword);

  CompletionStage<Boolean> createPassword(
      UUID userId, String email, String password, SqlTransaction transaction);

  CompletionStage<Boolean> passwordResetRequestExists(UUID token);

  default String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(13));
  }
}
