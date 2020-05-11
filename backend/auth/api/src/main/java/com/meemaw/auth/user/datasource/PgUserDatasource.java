package com.meemaw.auth.user.datasource;

import com.meemaw.auth.signup.model.SignupRequest;
import com.meemaw.shared.auth.Organization;
import com.meemaw.shared.auth.UserDTO;
import com.meemaw.shared.auth.UserRole;
import com.meemaw.shared.pg.PgError;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import io.vertx.pgclient.PgException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PgUserDatasource implements UserDatasource {

  @Inject PgPool pgPool;

  private static final String INSERT_USER_RAW_SQL =
      "INSERT INTO auth.user(email, org, role) VALUES($1, $2, $3) RETURNING id";

  public CompletionStage<UUID> createUser(
      Transaction transaction, String email, String org, UserRole role) {
    Tuple values = Tuple.of(email, org, role.toString());
    return transaction
        .preparedQuery(INSERT_USER_RAW_SQL, values)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID("id"))
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause();
              if (cause instanceof PgException) {
                PgException pgException = (PgException) cause;
                if (pgException.getCode().equals(PgError.UNIQUE_VIOLATION.getCode())) {
                  log.error("Email already exists email={} org={}", email, org);
                  throw Boom.status(Response.Status.CONFLICT)
                      .message("Email already exists")
                      .exception();
                }
              }
              log.error("Failed to create user email={} org={}", email, org, throwable);
              throw new DatabaseException(throwable);
            });
  }

  private static final String FIND_USER_BY_EMAIL_RAW_SQL =
      "SELECT * FROM auth.user WHERE email = $1";

  @Override
  public CompletionStage<Optional<UserDTO>> findUser(String email) {
    Tuple values = Tuple.of(email);
    return pgPool
        .preparedQuery(FIND_USER_BY_EMAIL_RAW_SQL, values)
        .thenApply(
            pgRowSet -> {
              if (!pgRowSet.iterator().hasNext()) {
                return Optional.empty();
              }
              Row row = pgRowSet.iterator().next();
              return Optional.of(
                  new UserDTO(
                      row.getUUID("id"),
                      row.getString("email"),
                      UserRole.valueOf(row.getString("role")),
                      row.getString("org")));
            });
  }

  public CompletionStage<SignupRequest> createUser(
      Transaction transaction, SignupRequest signupRequest) {
    String email = signupRequest.email();
    String org = signupRequest.org();
    return createUser(transaction, email, org, UserRole.ADMIN).thenApply(signupRequest::userId);
  }

  private static final String INSERT_ORG_RAW_SQL = "INSERT INTO auth.org(id) VALUES($1)";

  public CompletionStage<SignupRequest> createOrganization(
      Transaction transaction, SignupRequest signupRequest) {
    String orgID = Organization.identifier();
    Tuple values = Tuple.of(orgID);

    return transaction
        .preparedQuery(INSERT_ORG_RAW_SQL, values)
        .thenApply(pgRowSet -> signupRequest.org(orgID))
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause();
              if (cause instanceof PgException) {
                PgException pgException = (PgException) cause;
                if (pgException.getCode().equals(PgError.UNIQUE_VIOLATION.getCode())) {
                  log.error("Organization already exists orgID={}", orgID);
                  throw Boom.status(Response.Status.CONFLICT)
                      .message("Organization already exists")
                      .exception();
                }
              }
              log.error("Failed to create organization orgID={}", orgID, throwable);
              throw new DatabaseException(throwable);
            });
  }
}
