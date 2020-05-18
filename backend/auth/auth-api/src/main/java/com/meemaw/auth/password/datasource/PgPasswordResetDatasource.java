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
      "DELETE FROM auth.password_reset_request WHERE token = $1 AND email = $2 AND org = $3";

  private static final String FIND_RAW_SQL =
      "SELECT * FROM auth.password_reset_request WHERE token = $1 AND email = $2 AND org = $3";

  private static final String CREATE_RAW_SQL =
      "INSERT INTO auth.password_reset_request(email, user_id, org) VALUES($1, $2, $3) RETURNING token, created_at";

  @Override
  public CompletionStage<Boolean> delete(
      Transaction transaction, UUID token, String email, String org) {
    Tuple values = Tuple.of(token, email, org);
    return transaction.preparedQuery(DELETE_RAW_SQL, values).thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Optional<PasswordResetRequest>> find(
      UUID token, String email, String org) {
    Tuple values = Tuple.of(token, email, org);
    return pgPool.preparedQuery(FIND_RAW_SQL, values).thenApply(this::resetRequestFromRowSet);
  }

  @Override
  public CompletionStage<PasswordResetRequest> create(
      Transaction transaction, String email, UUID userId, String org) {
    Tuple values = Tuple.of(email, userId, org);
    return transaction
        .preparedQuery(CREATE_RAW_SQL, values)
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID("token");
              OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
              return new PasswordResetRequest(token, userId, email, org, createdAt);
            });
  }

  @Override
  public CompletionStage<Boolean> exists(String email, String org, UUID token) {
    return find(token, email, org).thenApply(Optional::isPresent);
  }

  private Optional<PasswordResetRequest> resetRequestFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(PasswordResetRequest.fromSqlRow(rowSet.iterator().next()));
  }
}
