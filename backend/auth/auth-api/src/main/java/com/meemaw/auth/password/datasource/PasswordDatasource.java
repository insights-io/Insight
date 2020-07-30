package com.meemaw.auth.password.datasource;

import com.meemaw.auth.user.model.UserWithHashedPassword;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordDatasource {

  CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, SqlTransaction transaction);

  CompletionStage<Boolean> storePassword(UUID userId, String hashedPassword);

  CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email);
}
