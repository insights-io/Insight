package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.user.datasource.sql.SqlTfaSetupTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.SqlTfaSetupTable.METHOD;
import static com.meemaw.auth.user.datasource.sql.SqlTfaSetupTable.PARAMS;
import static com.meemaw.auth.user.datasource.sql.SqlTfaSetupTable.TABLE;
import static com.meemaw.auth.user.datasource.sql.SqlTfaSetupTable.USER_ID;

import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.model.TfaSetup;
import com.meemaw.auth.tfa.sms.model.SmsTfaSetup;
import com.meemaw.auth.tfa.totp.model.TfaTotpSetup;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
  public CompletionStage<List<TfaSetup>> list(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(SqlUserTfaDatasource::mapTfaSetupList);
  }

  @Override
  @Traced
  public CompletionStage<Optional<TfaSetup>> get(UUID userId, TfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(TABLE)
            .where(USER_ID.eq(userId).and(METHOD.eq(method.getKey())));
    return sqlPool.execute(query).thenApply(SqlUserTfaDatasource::maybeMapTfaSetup);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID userId, TfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(USER_ID.eq(userId).and(METHOD.eq(method.getKey())))
            .returning(CREATED_AT);
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<TfaSetup> store(
      UUID userId, TfaMethod method, JsonObject params, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(SqlTfaSetupTable.INSERT_FIELDS)
            .values(userId, params, method.getKey())
            .returning(CREATED_AT);

    return transaction
        .query(query)
        .thenApply(rows -> rows.iterator().next())
        .thenApply(
            row -> {
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              if (method.equals(TfaMethod.TOTP)) {
                return new TfaTotpSetup(params.getString("secret"), createdAt, userId);
              }
              return new SmsTfaSetup(createdAt, userId);
            });
  }

  public static Optional<TfaSetup> maybeMapTfaSetup(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row row = rows.iterator().next();
    return Optional.of(mapTfaSetup(row));
  }

  public static TfaSetup mapTfaSetup(Row row) {
    TfaMethod method = TfaMethod.fromString(row.getString(METHOD.getName()));
    OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
    UUID userId = row.getUUID(USER_ID.getName());

    if (method.equals(TfaMethod.TOTP)) {
      JsonObject params = (JsonObject) row.getValue(PARAMS.getName());
      return new TfaTotpSetup(params.getString("secret"), createdAt, userId);
    }

    return new SmsTfaSetup(createdAt, userId);
  }

  public static List<TfaSetup> mapTfaSetupList(RowSet<Row> rows) {
    List<TfaSetup> tfaSetups = new ArrayList<>(rows.size());
    for (Row row : rows) {
      tfaSetups.add(mapTfaSetup(row));
    }
    return tfaSetups;
  }
}
