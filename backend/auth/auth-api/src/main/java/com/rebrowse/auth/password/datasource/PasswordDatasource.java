package com.rebrowse.auth.password.datasource;

import com.rebrowse.shared.sql.client.SqlTransaction;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface PasswordDatasource {

  CompletionStage<OffsetDateTime> storePassword(
      UUID userId, String hashedPassword, SqlTransaction transaction);

  CompletionStage<OffsetDateTime> storePassword(UUID userId, String hashedPassword);
}
