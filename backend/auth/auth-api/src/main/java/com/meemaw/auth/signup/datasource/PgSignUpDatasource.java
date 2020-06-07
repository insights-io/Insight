package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.shared.rest.exception.DatabaseException;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
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
public class PgSignUpDatasource implements SignUpDatasource {

  @Inject PgPool pgPool;

  private static final String INSERT_SIGN_UP_RAW_SQL =
      "INSERT INTO auth.sign_up_request(email, hashed_password, full_name, company, phone_number, referer) VALUES($1, $2, $3, $4, $5, $6) RETURNING token";

  private static final String SELECT_SIGN_UP_RAW_SQL =
      "SELECT * FROM auth.sign_up_request WHERE token = $1";

  private static final String DELETE_SIGN_UP_RAW_SQL =
      "DELETE FROM auth.sign_up_request WHERE token = $1";

  private static final String SELECT_EMAIL_TAKEN_RAW_SQL =
      "SELECT COUNT(*) FROM auth.user FULL OUTER JOIN auth.sign_up_request ON auth.user.email = auth.sign_up_request.email WHERE auth.user.email = $1 OR auth.sign_up_request.email = $1";

  @Override
  public CompletionStage<UUID> createSignUpRequest(
      SignUpRequest signUpRequest, Transaction transaction) {

    return transaction
        .preparedQuery(INSERT_SIGN_UP_RAW_SQL)
        .execute(
            Tuple.of(
                signUpRequest.getEmail(),
                signUpRequest.getHashedPassword(),
                signUpRequest.getFullName(),
                signUpRequest.getCompany(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getReferer()))
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID("token"))
        .exceptionally(
            throwable -> {
              log.error("Failed to create sign up request", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token) {
    return pgPool.begin().thenCompose(transaction -> findSignUpRequest(token, transaction));
  }

  @Override
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(
      UUID token, Transaction transaction) {
    return transaction
        .preparedQuery(SELECT_SIGN_UP_RAW_SQL)
        .execute(Tuple.of(token))
        .exceptionally(
            throwable -> {
              log.error("Failed to fetch sign up request", throwable);
              throw new DatabaseException(throwable);
            })
        .thenApply(
            pgRowSet -> {
              if (!pgRowSet.iterator().hasNext()) {
                return Optional.empty();
              }

              Row row = pgRowSet.iterator().next();
              return Optional.of(mapSignUpRequest(row));
            });
  }

  @Override
  public CompletionStage<Boolean> deleteSignUpRequest(UUID token, Transaction transaction) {
    return transaction
        .preparedQuery(DELETE_SIGN_UP_RAW_SQL)
        .execute(Tuple.of(token))
        .thenApply(pgRowSet -> true)
        .exceptionally(
            throwable -> {
              log.error("Failed to delete sign up request", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  public CompletionStage<Boolean> selectIsEmailTaken(String email, Transaction transaction) {
    return transaction
        .preparedQuery(SELECT_EMAIL_TAKEN_RAW_SQL)
        .execute(Tuple.of(email))
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getInteger("count") > 0);
  }

  /**
   * Map SQL row to SignUpRequest.
   *
   * @param row SQL row
   * @return mapped SignUpRequest
   */
  public static SignUpRequest mapSignUpRequest(Row row) {
    return new SignUpRequest(
        row.getUUID("token"),
        row.getString("email"),
        row.getString("hashed_password"),
        row.getString("full_name"),
        row.getString("company"),
        row.getString("phone_number"),
        row.getString("referer"),
        row.getOffsetDateTime("created_at"));
  }
}
