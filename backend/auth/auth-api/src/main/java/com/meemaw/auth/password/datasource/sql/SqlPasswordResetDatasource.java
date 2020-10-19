package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.CREATED_AT;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.EMAIL;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.INSERT_FIELDS;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.TOKEN;
import static com.meemaw.auth.password.datasource.sql.SqlPasswordResetRequestTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordResetDatasource;
import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.time.OffsetDateTime;
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
public class SqlPasswordResetDatasource implements PasswordResetDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Boolean> deletePasswordResetRequest(
      UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().delete(TABLE).where(TOKEN.eq(token));
    return transaction.execute(query).thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Optional<PasswordResetRequest>> findPasswordResetRequest(UUID token) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
    return sqlPool.execute(query).thenApply(this::mapMaybePasswordResetRequest);
  }

  @Override
  @Traced
  public CompletionStage<PasswordResetRequest> createPasswordResetRequest(
      String email, UUID userId, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, userId)
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .execute(query)
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID(TOKEN.getName());
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              return new PasswordResetRequest(token, userId, email, createdAt);
            });
  }

  private Optional<PasswordResetRequest> mapMaybePasswordResetRequest(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapPasswordResetRequest(rows.iterator().next()));
  }

  public static PasswordResetRequest mapPasswordResetRequest(Row row) {
    return new PasswordResetRequest(
        row.getUUID(TOKEN.getName()),
        row.getUUID(USER_ID.getName()),
        row.getString(EMAIL.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
