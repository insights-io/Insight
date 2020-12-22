package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.CREATED_AT;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.datasource.AbstractSqlDatasource;
import io.vertx.mutiny.sqlclient.Row;
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
public class SqlPasswordDatasource extends AbstractSqlDatasource<OffsetDateTime>
    implements PasswordDatasource {

  @Inject SqlPool sqlPool;

  public static OffsetDateTime map(Row row) {
    return row.getOffsetDateTime(CREATED_AT.getName());
  }

  @Override
  public OffsetDateTime fromSql(Row row) {
    return SqlPasswordDatasource.map(row);
  }

  @Override
  @Traced
  public CompletionStage<OffsetDateTime> storePassword(
      UUID userId, String hash, SqlTransaction transaction) {
    return transaction.execute(insertPasswordQuery(userId, hash)).thenApply(this::expectOne);
  }

  @Override
  public CompletionStage<OffsetDateTime> storePassword(UUID userId, String hash) {
    return sqlPool.execute(insertPasswordQuery(userId, hash)).thenApply(this::expectOne);
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
