package com.rebrowse.auth.signup.datasource.sql;

import com.rebrowse.auth.signup.datasource.SignUpDatasource;
import com.rebrowse.auth.signup.model.SignUpRequest;
import com.rebrowse.auth.user.datasource.sql.SqlUserTable;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
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
    String referrer = row.getString(SqlSignUpRequestTable.REDIRECT.getName());

    return new SignUpRequest(
        row.getUUID(SqlSignUpRequestTable.TOKEN.getName()),
        row.getString(SqlSignUpRequestTable.EMAIL.getName()),
        row.getString(SqlSignUpRequestTable.HASHED_PASSWORD.getName()),
        row.getString(SqlSignUpRequestTable.FULL_NAME.getName()),
        row.getString(SqlSignUpRequestTable.COMPANY.getName()),
        Optional.ofNullable(phoneNumber).map(p -> p.mapTo(PhoneNumberDTO.class)).orElse(null),
        Optional.ofNullable(referrer).map(RequestUtils::sneakyUrl).orElse(null),
        row.getOffsetDateTime(SqlSignUpRequestTable.CREATED_AT.getName()));
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
            .insertInto(SqlSignUpRequestTable.TABLE)
            .columns(SqlSignUpRequestTable.INSERT_FIELDS)
            .values(
                signUpRequest.getEmail(),
                signUpRequest.getHashedPassword(),
                signUpRequest.getFullName(),
                signUpRequest.getCompany(),
                JsonObject.mapFrom(signUpRequest.getPhoneNumber()),
                signUpRequest.getRedirect())
            .returning(SqlSignUpRequestTable.AUTO_GENERATED_FIELDS);

    return transaction
        .execute(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getUUID(SqlSignUpRequestTable.TOKEN.getName()));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> retrieve(UUID token) {
    return sqlPool.beginTransaction().thenCompose(transaction -> retrieve(token, transaction));
  }

  @Override
  @Traced
  public CompletionStage<Optional<SignUpRequest>> retrieve(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(SqlSignUpRequestTable.TABLE).where(SqlSignUpRequestTable.TOKEN.eq(token));
    return transaction.execute(query).thenApply(this::findOne);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().deleteFrom(SqlSignUpRequestTable.TABLE).where(SqlSignUpRequestTable.TOKEN.eq(token)).returning(SqlSignUpRequestTable.TOKEN);
    return transaction.execute(query).thenApply(this::hasNext);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> retrieveIsEmailTaken(String email, SqlTransaction transaction) {
    Field<String> userEmail = SqlUserTable.tableField(SqlUserTable.EMAIL);
    Field<String> signUpRequestEmail = SqlSignUpRequestTable.tableField(SqlSignUpRequestTable.EMAIL);

    Query query =
        sqlPool
            .getContext()
            .selectCount()
            .from(SqlUserTable.TABLE.fullOuterJoin(SqlSignUpRequestTable.TABLE).on(userEmail.eq(signUpRequestEmail)))
            .where(userEmail.eq(email).or(signUpRequestEmail.eq(email)));

    return transaction
        .execute(query)
        .thenApply(pgRowSet -> pgRowSet.iterator().next().getInteger("count") > 0);
  }
}
