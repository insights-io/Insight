package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.dto.SignupRequestDTO;
import com.meemaw.shared.auth.UserDTO;
import com.meemaw.shared.rest.exception.DatabaseException;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PgSignupDatasource implements SignupDatasource {

  @Inject
  PgPool pgPool;

  private static final String INSERT_SIGNUP_RAW_SQL = "INSERT INTO auth.signup(user_email, org, user_id) VALUES($1, $2, $3) RETURNING token, created_at";

  public CompletionStage<SignupRequestDTO> create(Transaction transaction,
      UserDTO userDTO) {
    String email = userDTO.getEmail();
    String org = userDTO.getOrg();
    UUID userId = userDTO.getId();
    Tuple values = Tuple.of(email, org, userId);

    return transaction.preparedQuery(INSERT_SIGNUP_RAW_SQL, values)
        .thenApply(pgRowSet -> {
          Row row = pgRowSet.iterator().next();
          UUID token = row.getUUID("token");
          OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
          return new SignupRequestDTO(email, org, token, userId, createdAt);
        })
        .exceptionally(throwable -> {
          log.error("Failed to create signup email={} userId={} org={}", email, userId, org,
              throwable);
          throw new DatabaseException();
        });
  }

  public CompletionStage<Boolean> exists(String email, String org, UUID token) {
    return find(email, org, token).thenApply(Optional::isPresent)
        .exceptionally(throwable -> {
          log.error("Failed to verify signup exists email={} org={} token={}", email, org, token,
              throwable);
          throw new DatabaseException();
        });
  }

  private static final String FIND_SIGNUP_RAW_SQL = "SELECT * FROM auth.signup WHERE user_email = $1 AND org = $2 AND token = $3";

  public CompletionStage<Optional<SignupRequestDTO>> find(String email, String org, UUID token) {
    Tuple values = Tuple.of(email, org, token);
    return pgPool.preparedQuery(FIND_SIGNUP_RAW_SQL, values)
        .thenApply(pgRowSet -> {
          if (!pgRowSet.iterator().hasNext()) {
            return Optional.empty();
          }
          Row row = pgRowSet.iterator().next();
          return Optional.of(new SignupRequestDTO(
              row.getString("user_email"),
              row.getString("org"),
              row.getUUID("token"),
              row.getUUID("user_id"),
              row.getOffsetDateTime("created_at")));
        });
  }

  private static final String DELETE_SIGNUP_RAW_SQL = "DELETE FROM auth.signup WHERE user_email = $1 AND org = $2 AND user_id = $3";

  public CompletionStage<Boolean> delete(Transaction transaction, String email, String orgId,
      UUID userId) {
    Tuple values = Tuple.of(email, orgId, userId);
    return transaction.preparedQuery(DELETE_SIGNUP_RAW_SQL, values).thenApply(pgRowSet -> true);
  }

}
