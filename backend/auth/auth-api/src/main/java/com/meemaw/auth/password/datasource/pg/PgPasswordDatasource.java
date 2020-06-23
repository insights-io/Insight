package com.meemaw.auth.password.datasource.pg;

import static com.meemaw.auth.password.datasource.pg.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.pg.PasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.pg.PasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.user.datasource.pg.PgUserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserWithHashedPassword;
import com.meemaw.shared.sql.SQLContext;
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
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgPasswordDatasource implements PasswordDatasource {

  @Inject PgPool pgPool;

  private static final String FIND_USER_WITH_ACTIVE_PASSWORD_RAW_SQL =
      "SELECT auth.user.id, auth.user.email, auth.user.full_name, auth.user.organization_id, auth.user.role, auth.user.created_at, auth.password.hash"
          + " FROM auth.user LEFT JOIN auth.password ON auth.user.id = auth.password.user_id"
          + " WHERE email = $1 ORDER BY auth.password.created_at DESC LIMIT 1";

  @Override
  @Traced
  public CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, Transaction transaction) {
    Query query =
        SQLContext.POSTGRES.insertInto(TABLE).columns(USER_ID, HASH).values(userId, hashedPassword);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email) {
    return pgPool
        .preparedQuery(FIND_USER_WITH_ACTIVE_PASSWORD_RAW_SQL)
        .execute(Tuple.of(email))
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
    AuthUser user = PgUserDatasource.mapUser(row);
    return new UserWithHashedPassword(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getOrganizationId(),
        user.getCreatedAt(),
        row.getString(HASH.getName()));
  }
}
