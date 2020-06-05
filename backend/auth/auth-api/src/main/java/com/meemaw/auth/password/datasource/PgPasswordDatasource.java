package com.meemaw.auth.password.datasource;

import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.UserWithHashedPassword;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PgPasswordDatasource implements PasswordDatasource {

  @Inject PgPool pgPool;

  private static final String INSERT_PASSWORD_RAW_SQL =
      "INSERT INTO auth.password(user_id, hash) VALUES($1, $2)";

  private static final String FIND_USER_WITH_ACTIVE_PASSWORD_RAW_SQL =
      "SELECT auth.user.id, auth.user.email, auth.user.full_name, auth.user.org_id, auth.user.role, auth.user.created_at, auth.password.hash"
          + " FROM auth.user LEFT JOIN auth.password ON auth.user.id = auth.password.user_id"
          + " WHERE email = $1 ORDER BY auth.password.created_at DESC LIMIT 1";

  @Override
  public CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, Transaction transaction) {
    return transaction
        .preparedQuery(INSERT_PASSWORD_RAW_SQL, Tuple.of(userId, hashedPassword))
        .thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email) {
    return pgPool
        .preparedQuery(FIND_USER_WITH_ACTIVE_PASSWORD_RAW_SQL, Tuple.of(email))
        .thenApply(this::userWithPasswordFromRowSet);
  }

  private Optional<UserWithHashedPassword> userWithPasswordFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapUserWithHashedPassword(rowSet.iterator().next()));
  }

  /**
   * Map sql row to UserWithHashedPassword.
   *
   * @param row sql row
   * @return mapped UserWithHashedPassword
   */
  public static UserWithHashedPassword mapUserWithHashedPassword(Row row) {
    return new UserWithHashedPassword(
        row.getUUID("id"),
        row.getString("email"),
        row.getString("full_name"),
        UserRole.valueOf(row.getString("role")),
        row.getString("org_id"),
        row.getOffsetDateTime("created_at"),
        row.getString("hash"));
  }
}
