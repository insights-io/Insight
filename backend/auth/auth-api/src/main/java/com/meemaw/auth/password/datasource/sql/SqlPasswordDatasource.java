package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.CREATED_AT;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPasswordDatasource implements PasswordDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<OffsetDateTime> storePassword(
      UUID userId, String hash, SqlTransaction transaction) {
    return transaction.query(insertPasswordQuery(userId, hash)).thenApply(this::mapStoredPassword);
  }

  @Override
  public CompletionStage<OffsetDateTime> storePassword(UUID userId, String hash) {
    return sqlPool.execute(insertPasswordQuery(userId, hash)).thenApply(this::mapStoredPassword);
  }

  private OffsetDateTime mapStoredPassword(RowSet<Row> rows) {
    return rows.iterator().next().getOffsetDateTime(CREATED_AT.getName());
  }

  private Query insertPasswordQuery(UUID userId, String hash) {
    return sqlPool
        .getContext()
        .insertInto(TABLE)
        .columns(USER_ID, HASH)
        .values(userId, hash)
        .returning(CREATED_AT);
  }
}
