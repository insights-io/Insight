package com.meemaw.auth.user.datasource;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.exception.DatabaseException;
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

@ApplicationScoped
@Slf4j
public class PgUserDatasource implements UserDatasource {

  @Inject PgPool pgPool;

  private static final String CREATE_USER_RAW_SQL =
      "INSERT INTO auth.user(email, full_name, organization_id, role) VALUES($1, $2, $3, $4) RETURNING id, created_at";

  private static final String FIND_USER_BY_EMAIL_RAW_SQL =
      "SELECT * FROM auth.user WHERE email = $1";

  @Override
  public CompletionStage<AuthUser> createUser(
      String email, String fullName, String org, UserRole role, Transaction transaction) {
    return transaction
        .preparedQuery(CREATE_USER_RAW_SQL)
        .execute(Tuple.of(email, fullName, org, role.toString()))
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              AuthUser user =
                  new UserDTO(
                      row.getUUID("id"),
                      email,
                      fullName,
                      role,
                      org,
                      row.getOffsetDateTime("created_at"));
              return user;
            })
        .exceptionally(this::onCreateUserException);
  }

  private AuthUser onCreateUserException(Throwable throwable) {
    log.error("Failed to create user", throwable);
    throw new DatabaseException(throwable);
  }

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String email) {
    return pgPool
        .preparedQuery(FIND_USER_BY_EMAIL_RAW_SQL)
        .execute(Tuple.of(email))
        .thenApply(this::onFindUser)
        .exceptionally(this::onFindUserException);
  }

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String email, Transaction transaction) {
    return transaction
        .preparedQuery(FIND_USER_BY_EMAIL_RAW_SQL)
        .execute(Tuple.of(email))
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
    return Optional.of(mapUserDTO(row));
  }

  private AuthUser mapUserDTO(Row row) {
    return new UserDTO(
        row.getUUID("id"),
        row.getString("email"),
        row.getString("full_name"),
        UserRole.valueOf(row.getString("role")),
        row.getString("organization_id"),
        row.getOffsetDateTime("created_at"));
  }
}
