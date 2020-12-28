package com.meemaw.auth.signup.datasource.sql;

import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.COMPANY;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.CREATED_AT;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.EMAIL;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.FULL_NAME;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.HASHED_PASSWORD;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.INSERT_FIELDS;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.REFERRER;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.TABLE;
import static com.meemaw.auth.signup.datasource.sql.SqlSignUpRequestTable.TOKEN;

import com.meemaw.auth.signup.datasource.SignUpDatasource;
import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.user.datasource.sql.SqlUserTable;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.datasource.AbstractSqlDatasource;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
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
public class SqlSignUpDatasource extends AbstractSqlDatasource<SignUpRequest>
    implements SignUpDatasource {

  @Inject SqlPool sqlPool;

  public static SignUpRequest map(Row row) {
    JsonObject phoneNumber = (JsonObject) row.getValue(SqlUserTable.PHONE_NUMBER.getName());
    String referrer = row.getString(REFERRER.getName());

    return new SignUpRequest(
        row.getUUID(TOKEN.getName()),
        row.getString(EMAIL.getName()),
        row.getString(HASHED_PASSWORD.getName()),
        row.getString(FULL_NAME.getName()),
        row.getString(COMPANY.getName()),
        Optional.ofNullable(phoneNumber).map(p -> p.mapTo(PhoneNumberDTO.class)).orElse(null),
        Optional.ofNullable(referrer).map(RequestUtils::sneakyUrl).orElse(null),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }

  @Override
  public SignUpRequest fromSql(Row row) {
    return SqlSignUpDatasource.map(row);
  }

  @Override
  @Traced
  public CompletionStage<UUID> create(SignUpRequest signUpRequest, SqlTransaction transaction) {
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
                JsonObject.mapFrom(signUpRequest.getPhoneNumber()),
                signUpRequest.getReferrer())
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .execute(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID(TOKEN.getName()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> retrieve(UUID token) {
    return sqlPool.beginTransaction().thenCompose(transaction -> retrieve(token, transaction));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> retrieve(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
    return transaction.execute(query).thenApply(this::findOne);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(TOKEN.eq(token)).returning(TOKEN);
    return transaction.execute(query).thenApply(this::hasNext);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> retrieveIsEmailTaken(String email, SqlTransaction transaction) {
    Field<String> userEmail = SqlUserTable.tableField(SqlUserTable.EMAIL);
    Field<String> signUpRequestEmail = SqlSignUpRequestTable.tableField(EMAIL);

    Query query =
        sqlPool
            .getContext()
            .selectCount()
            .from(SqlUserTable.TABLE.fullOuterJoin(TABLE).on(userEmail.eq(signUpRequestEmail)))
            .where(userEmail.eq(email).or(signUpRequestEmail.eq(email)));

    return transaction
        .execute(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getInteger("count") > 0);
  }
}
