package com.meemaw.auth.password.datasource;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.auth.password.model.PasswordResetRequest;
import com.meemaw.shared.sql.SQLContext;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Table;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgPasswordResetDatasource implements PasswordResetDatasource {

  @Inject PgPool pgPool;

  private static final Table<?> TABLE = table("auth.password_reset_request");

  private static final Field<UUID> TOKEN = field("token", UUID.class);
  private static final Field<UUID> USER_ID = field("user_id", UUID.class);
  private static final Field<String> EMAIL = field("email", String.class);
  private static final Field<OffsetDateTime> CREATED_AT = field("created_at", OffsetDateTime.class);

  private static final Set<Field<?>> INSERT_FIELDS = new HashSet<>(List.of(EMAIL, USER_ID));
  private static final Set<Field<?>> AUTO_GENERATED_FIELDS =
      new HashSet<>(List.of(TOKEN, CREATED_AT));

  @Override
  @Traced
  public CompletionStage<Boolean> deletePasswordResetRequest(UUID token, Transaction transaction) {
    Query query = SQLContext.POSTGRES.delete(TABLE).where(TOKEN.eq(token));
    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Optional<PasswordResetRequest>> findPasswordResetRequest(UUID token) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(TOKEN.eq(token));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::mapMaybePasswordResetRequest);
  }

  @Override
  @Traced
  public CompletionStage<PasswordResetRequest> createPasswordResetRequest(
      String email, UUID userId, Transaction transaction) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, userId)
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID(TOKEN.getName());
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              return new PasswordResetRequest(token, userId, email, createdAt);
            });
  }

  private Optional<PasswordResetRequest> mapMaybePasswordResetRequest(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapPasswordResetRequest(rowSet.iterator().next()));
  }

  /**
   * Map SQL row to PasswordResetRequest.
   *
   * @param row SQL row
   * @return mapped PasswordResetRequest
   */
  public static PasswordResetRequest mapPasswordResetRequest(Row row) {
    return new PasswordResetRequest(
        row.getUUID(TOKEN.getName()),
        row.getUUID(USER_ID.getName()),
        row.getString(EMAIL.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
