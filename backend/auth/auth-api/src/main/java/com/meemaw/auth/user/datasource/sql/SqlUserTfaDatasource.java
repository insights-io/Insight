package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.user.datasource.sql.TFASetupTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.SECRET;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.TABLE;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.USER_ID;

import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.TfaSetup;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlUserTfaDatasource implements UserTfaDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Optional<TfaSetup>> get(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(SqlUserTfaDatasource::maybeMapTfaSetup);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID userId) {
    Query query =
        sqlPool.getContext().deleteFrom(TABLE).where(USER_ID.eq(userId)).returning(CREATED_AT);
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<TfaSetup> store(UUID userId, String secret, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(TFASetupTable.INSERT_FIELDS)
            .values(userId, secret)
            .returning(CREATED_AT);

    return transaction
        .query(query)
        .thenApply(rows -> rows.iterator().next())
        .thenApply(row -> new TfaSetup(secret, row.getOffsetDateTime(CREATED_AT.getName())));
  }

  public static Optional<TfaSetup> maybeMapTfaSetup(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row row = rows.iterator().next();
    return Optional.of(mapTfaSetup(row));
  }

  public static TfaSetup mapTfaSetup(Row row) {
    return new TfaSetup(
        row.getString(SECRET.getName()), row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
