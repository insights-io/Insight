package com.meemaw.auth.user.datasource.pg;

import static com.meemaw.auth.user.datasource.pg.UserTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.pg.UserTable.EMAIL;
import static com.meemaw.auth.user.datasource.pg.UserTable.FIELDS;
import static com.meemaw.auth.user.datasource.pg.UserTable.FULL_NAME;
import static com.meemaw.auth.user.datasource.pg.UserTable.ID;
import static com.meemaw.auth.user.datasource.pg.UserTable.INSERT_FIELDS;
import static com.meemaw.auth.user.datasource.pg.UserTable.ORGANIZATION_ID;
import static com.meemaw.auth.user.datasource.pg.UserTable.ROLE;
import static com.meemaw.auth.user.datasource.pg.UserTable.TABLE;

import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.sql.SQLContext;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgUserDatasource implements UserDatasource {

  @Inject PgPool pgPool;

  @Override
  @Traced
  public CompletionStage<AuthUser> createUser(
      String email,
      String fullName,
      String organizationId,
      UserRole role,
      Transaction transaction) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(email, fullName, organizationId, role.toString())
            .returning(FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> mapUser(pgRowSet.iterator().next()))
        .exceptionally(this::onCreateUserException);
  }

  private AuthUser onCreateUserException(Throwable throwable) {
    log.error("Failed to create user", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(String email) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(EMAIL.eq(email));

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::onFindUser)
        .exceptionally(this::onFindUserException);
  }

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(String email, Transaction transaction) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(EMAIL.eq(email));

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::onFindUser)
        .exceptionally(this::onFindUserException);
  }

  private Optional<AuthUser> onFindUserException(Throwable throwable) {
    log.error("Failed to find user", throwable);
    throw new DatabaseException(throwable);
  }

  private Optional<AuthUser> onFindUser(RowSet<Row> pgRowSet) {
    if (!pgRowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    Row row = pgRowSet.iterator().next();
    return Optional.of(mapUser(row));
  }

  /**
   * Map SQL row to AuthUser.
   *
   * @param row sql row
   * @return mapped AuthUser
   */
  public static AuthUser mapUser(Row row) {
    return new UserDTO(
        row.getUUID(ID.getName()),
        row.getString(EMAIL.getName()),
        row.getString(FULL_NAME.getName()),
        UserRole.valueOf(row.getString(ROLE.getName())),
        row.getString(ORGANIZATION_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
