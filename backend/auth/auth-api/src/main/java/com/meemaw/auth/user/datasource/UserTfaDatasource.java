package com.meemaw.auth.user.datasource;

import com.meemaw.auth.user.model.TfaSetup;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface UserTfaDatasource {

  CompletionStage<Optional<TfaSetup>> get(UUID userId);

  CompletionStage<Boolean> delete(UUID userId);

  CompletionStage<TfaSetup> store(UUID userId, String secret, SqlTransaction sqlTransaction);
}
