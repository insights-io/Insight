package com.meemaw.auth.password.datasource;

import com.meemaw.auth.user.model.UserWithHashedPasswordDTO;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordDatasource {

  CompletionStage<Boolean> create(Transaction transaction, UUID userId, String hashedPassword);

  CompletionStage<Optional<UserWithHashedPasswordDTO>> findUserWithPassword(String email);

}
