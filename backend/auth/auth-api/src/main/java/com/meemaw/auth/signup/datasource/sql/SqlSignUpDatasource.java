package com.meemaw.auth.signup.datasource.sql;

import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.COMPANY;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.CREATED_AT;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.EMAIL;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.FULL_NAME;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.HASHED_PASSWORD;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.INSERT_FIELDS;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.PHONE_NUMBER;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.REFERER;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.TABLE;
import static com.meemaw.auth.signup.datasource.sql.SignUpRequestTable.TOKEN;

import com.meemaw.auth.signup.datasource.SignUpDatasource;
import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.user.datasource.sql.UserTable;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Field;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlSignUpDatasource implements SignUpDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<UUID> createSignUpRequest(
      SignUpRequest signUpRequest, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
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
        .query(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID(TOKEN.getName()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token) {
    return sqlPool
        .beginTransaction()
        .thenCompose(transaction -> findSignUpRequest(token, transaction));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> findSignUpRequest(
      UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
    return transaction.query(query).thenApply(SqlSignUpDatasource::maybeMapSignUpRequest);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> deleteSignUpRequest(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(TOKEN.eq(token));
    return transaction.query(query).thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> selectIsEmailTaken(String email, SqlTransaction transaction) {
    Field<String> userEmail = UserTable.tableField(UserTable.EMAIL);
    Field<String> signUpRequestEmail = SignUpRequestTable.tableField(EMAIL);

    Query query =
        sqlPool
            .getContext()
            .selectCount()
            .from(UserTable.TABLE.fullOuterJoin(TABLE).on(userEmail.eq(signUpRequestEmail)))
            .where(userEmail.eq(email).or(signUpRequestEmail.eq(email)));

    return transaction
        .query(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getInteger("count") > 0);
  }

  private static Optional<SignUpRequest> maybeMapSignUpRequest(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapSignUpRequest(rows.iterator().next()));
  }

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
