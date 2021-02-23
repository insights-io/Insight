package com.rebrowse.auth.password.datasource.sql;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.password.datasource.PasswordResetDatasource;
import com.rebrowse.auth.password.model.PasswordResetRequest;
import com.rebrowse.auth.user.datasource.sql.SqlMfaConfigurationTable;
import com.rebrowse.auth.user.datasource.sql.SqlUserDatasource;
import com.rebrowse.auth.user.datasource.sql.SqlUserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import io.smallrye.mutiny.tuples.Tuple3;
import io.vertx.mutiny.sqlclient.Row;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPasswordResetDatasource extends AbstractSqlDatasource<PasswordResetRequest>
    implements PasswordResetDatasource {

  @Inject SqlPool sqlPool;

  public static PasswordResetRequest map(Row row) {
    return new PasswordResetRequest(
        row.getUUID(SqlPasswordResetRequestTable.TOKEN.getName()),
        row.getUUID(SqlPasswordResetRequestTable.USER_ID.getName()),
        row.getString(SqlPasswordResetRequestTable.EMAIL.getName()),
        RequestUtils.sneakyUrl(row.getString(SqlPasswordResetRequestTable.REDIRECT.getName())),
        row.getOffsetDateTime(SqlPasswordResetRequestTable.CREATED_AT.getName()));
  }

  @Override
  public PasswordResetRequest fromSql(Row row) {
    return SqlPasswordResetDatasource.map(row);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().delete(SqlPasswordResetRequestTable.TABLE).where(SqlPasswordResetRequestTable.TOKEN.eq(token));
    return transaction.execute(query).thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Boolean> exists(UUID token) {
    Query query = sqlPool.getContext().selectOne().from(SqlPasswordResetRequestTable.TABLE).where(SqlPasswordResetRequestTable.TOKEN.eq(token));
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<Optional<PasswordResetRequest>> retrieve(UUID token) {
    Query query = sqlPool.getContext().selectFrom(SqlPasswordResetRequestTable.TABLE).where(SqlPasswordResetRequestTable.TOKEN.eq(token));
    return sqlPool.execute(query).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<Tuple3<PasswordResetRequest, AuthUser, List<MfaMethod>>>>
      retrieveWithLoginInformation(UUID token) {
    Query query =
        sqlPool
            .getContext()
            .select(SqlPasswordResetRequestTable.WITH_LOGIN_INFORMATION_FIELDS)
            .from(
                SqlPasswordResetRequestTable.TABLE
                    .leftJoin(SqlUserTable.TABLE)
                    .on(
                        SqlUserTable.USER_TABLE_ID.eq(
                            SqlPasswordResetRequestTable.TABLE_ALIAS_USER_ID))
                    .leftJoin(SqlMfaConfigurationTable.TABLE_ALIAS)
                    .on(
                        SqlUserTable.USER_TABLE_ID.eq(
                            SqlMfaConfigurationTable.TABLE_ALIAS_USER_ID)))
            .where(SqlPasswordResetRequestTable.TOKEN.eq(token))
            .orderBy(SqlPasswordResetRequestTable.CREATED_AT_ALIAS.desc())
            .limit(MfaMethod.NUM_METHODS);

    return sqlPool
        .execute(query)
        .thenApply(
            rows -> {
              if (!rows.iterator().hasNext()) {
                return Optional.empty();
              }

              Row firstRow = rows.iterator().next();
              AuthUser user = SqlUserDatasource.mapUser(firstRow);
              PasswordResetRequest request = map(firstRow);

              List<MfaMethod> mfaMethods = new ArrayList<>(rows.size());
              for (Row row : rows) {
                Optional.ofNullable(row.getString(SqlMfaConfigurationTable.METHOD.getName()))
                    .ifPresent(method -> mfaMethods.add(MfaMethod.fromString(method)));
              }

              return Optional.of(Tuple3.of(request, user, mfaMethods));
            });
  }

  @Override
  @Traced
  public CompletionStage<PasswordResetRequest> create(
      String email, URL redirect, UUID userId, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(SqlPasswordResetRequestTable.TABLE)
            .columns(SqlPasswordResetRequestTable.INSERT_FIELDS)
            .values(email, redirect, userId)
            .returning(SqlPasswordResetRequestTable.AUTO_GENERATED_FIELDS);

    return transaction
        .execute(query)
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID(SqlPasswordResetRequestTable.TOKEN.getName());
              OffsetDateTime createdAt = row.getOffsetDateTime(SqlPasswordResetRequestTable.CREATED_AT.getName());
              return new PasswordResetRequest(token, userId, email, redirect, createdAt);
            });
  }
}
