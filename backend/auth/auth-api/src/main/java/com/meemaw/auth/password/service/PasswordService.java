package com.meemaw.auth.password.service;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.sql.client.SqlTransaction;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordService {

  CompletionStage<UserWithLoginInformation> verifyPassword(String email, String password);

  CompletionStage<Optional<AuthUser>> forgotPassword(String email, URL passwordResetBaseURL);

  CompletionStage<PasswordResetRequest> resetPassword(UUID token, String password);

  CompletionStage<OffsetDateTime> changePassword(
      UUID userId, String email, String organizationId, String currentPassword, String newPassword);

  CompletionStage<Boolean> createPassword(
      UUID userId, String email, String password, SqlTransaction transaction);

  CompletionStage<Boolean> passwordResetRequestExists(UUID token);

  String hashPassword(String password);
}
