package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SignUpDatasource {

  CompletionStage<UUID> create(SignUpRequest signUpRequest, SqlTransaction transaction);

  CompletionStage<Optional<SignUpRequest>> retrieve(UUID token);

  CompletionStage<Optional<SignUpRequest>> retrieve(UUID token, SqlTransaction transaction);

  CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction);

  CompletionStage<Boolean> retrieveIsEmailTaken(String email, SqlTransaction transaction);
}
