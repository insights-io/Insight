package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.dto.SignupRequestDTO;
import com.meemaw.auth.user.model.UserDTO;
import io.vertx.axle.sqlclient.Transaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SignupDatasource {

  CompletionStage<SignupRequestDTO> create(Transaction transaction, UserDTO userDTO);

  CompletionStage<Boolean> exists(String email, String org, UUID token);

  CompletionStage<Optional<SignupRequestDTO>> find(String email, String org, UUID token);

  CompletionStage<Boolean> delete(Transaction transaction, String email, String orgId, UUID userId);
}
