package com.meemaw.auth.password.datasource;

import com.meemaw.auth.password.model.PasswordResetRequest;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PgPasswordResetDatasource implements PasswordResetDatasource {

  @Inject PgPool pgPool;

  private static final String DELETE_RAW_SQL =
      "DELETE FROM auth.password_reset_request WHERE token = $1";

  private static final String FIND_RAW_SQL =
      "SELECT * FROM auth.password_reset_request WHERE token = $1";

  private static final String CREATE_RAW_SQL =
      "INSERT INTO auth.password_reset_request(email, user_id) VALUES($1, $2) RETURNING token, created_at";

  @Override
  public CompletionStage<Boolean> deletePasswordResetRequest(UUID token, Transaction transaction) {
    return transaction
        .preparedQuery(DELETE_RAW_SQL)
        .execute(Tuple.of(token))
        .thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Optional<PasswordResetRequest>> findPasswordResetRequest(UUID token) {
    return pgPool
        .preparedQuery(FIND_RAW_SQL)
        .execute(Tuple.of(token))
        .thenApply(this::mapMaybePasswordResetRequest);
  }

  @Override
  public CompletionStage<PasswordResetRequest> createPasswordResetRequest(
      String email, UUID userId, Transaction transaction) {
    return transaction
        .preparedQuery(CREATE_RAW_SQL)
        .execute(Tuple.of(email, userId))
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID("token");
              OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
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
        row.getUUID("token"),
        row.getUUID("user_id"),
        row.getString("email"),
        row.getOffsetDateTime("created_at"));
  }
}
