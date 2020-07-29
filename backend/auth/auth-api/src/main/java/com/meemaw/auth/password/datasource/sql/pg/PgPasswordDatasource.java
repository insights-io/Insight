package com.meemaw.auth.password.datasource.sql.pg;

import static com.meemaw.auth.password.datasource.sql.PasswordTable.CREATED_AT;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.auth.password.datasource.sql.PasswordTable;
import com.meemaw.auth.user.datasource.pg.PgUserDatasource;
import com.meemaw.auth.user.datasource.pg.UserTable;
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
  public CompletionStage<Boolean> storePassword(UUID userId, String hashedPassword) {
    Query query =
        SQLContext.POSTGRES.insertInto(TABLE).columns(USER_ID, HASH).values(userId, hashedPassword);

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Optional<UserWithHashedPassword>> findUserWithPassword(String email) {
    Query query =
        SQLContext.POSTGRES
            .selectFrom(UserTable.TABLE.leftJoin(TABLE).on(UserTable.ID.eq(USER_ID)))
            .where(UserTable.EMAIL.eq(email))
            .orderBy(PasswordTable.tableField(CREATED_AT).desc())
            .limit(1);

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
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
