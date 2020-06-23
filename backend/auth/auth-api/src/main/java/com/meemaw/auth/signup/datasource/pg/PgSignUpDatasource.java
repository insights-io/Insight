package com.meemaw.auth.signup.datasource.pg;

import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.COMPANY;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.CREATED_AT;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.EMAIL;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.FULL_NAME;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.HASHED_PASSWORD;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.INSERT_FIELDS;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.PHONE_NUMBER;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.REFERER;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.TABLE;
import static com.meemaw.auth.signup.datasource.pg.SignUpRequestTable.TOKEN;

import com.meemaw.auth.signup.datasource.SignUpDatasource;
import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.shared.rest.exception.DatabaseException;
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
public class PgSignUpDatasource implements SignUpDatasource {

  @Inject PgPool pgPool;

  private static final String SELECT_EMAIL_TAKEN_RAW_SQL =
      "SELECT COUNT(*) FROM auth.user FULL OUTER JOIN auth.sign_up_request ON auth.user.email = auth.sign_up_request.email WHERE auth.user.email = $1 OR auth.sign_up_request.email = $1";

  @Override
  @Traced
  public CompletionStage<UUID> createSignUpRequest(
      SignUpRequest signUpRequest, Transaction transaction) {
    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                signUpRequest.getEmail(),
                signUpRequest.getHashedPassword(),
                signUpRequest.getFullName(),
                signUpRequest.getCompany(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getReferer())
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID(TOKEN.getName()))
        .exceptionally(
            throwable -> {
              log.error("Failed to create sign up request", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token) {
    return pgPool.begin().thenCompose(transaction -> findSignUpRequest(token, transaction));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(
      UUID token, Transaction transaction) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(TOKEN.eq(token));

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(PgSignUpDatasource::maybeMapSignUpRequest)
        .exceptionally(
            throwable -> {
              log.error("Failed to fetch sign up request", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  @Traced
  public CompletionStage<Boolean> deleteSignUpRequest(UUID token, Transaction transaction) {
    Query query = SQLContext.POSTGRES.deleteFrom(TABLE).where(TOKEN.eq(token));
    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true)
        .exceptionally(
            throwable -> {
              log.error("Failed to delete sign up request", throwable);
              throw new DatabaseException(throwable);
            });
  }

  @Override
  @Traced
  public CompletionStage<Boolean> selectIsEmailTaken(String email, Transaction transaction) {
    return transaction
        .preparedQuery(SELECT_EMAIL_TAKEN_RAW_SQL)
        .execute(Tuple.of(email))
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getInteger("count") > 0);
  }

  private static Optional<SignUpRequest> maybeMapSignUpRequest(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapSignUpRequest(rowSet.iterator().next()));
  }

  /**
   * Map SQL row to SignUpRequest.
   *
   * @param row SQL row
   * @return mapped SignUpRequest
   */
  public static SignUpRequest mapSignUpRequest(Row row) {
    return new SignUpRequest(
        row.getUUID(TOKEN.getName()),
        row.getString(EMAIL.getName()),
        row.getString(HASHED_PASSWORD.getName()),
        row.getString(FULL_NAME.getName()),
        row.getString(COMPANY.getName()),
        row.getString(PHONE_NUMBER.getName()),
        row.getString(REFERER.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
