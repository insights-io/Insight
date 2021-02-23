package com.rebrowse.auth.user.datasource.sql;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.mfa.sms.model.SmsMfaSetup;
import com.rebrowse.auth.mfa.totp.model.MfaConfigurationDTO;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import java.time.OffsetDateTime;
import java.util.Collection;
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
public class SqlUserMfaDatasource extends AbstractSqlDatasource<MfaConfiguration>
    implements UserMfaDatasource {

  @Inject SqlPool sqlPool;

  public static MfaConfiguration mapConfiguration(Row row) {
    MfaMethod method =
        MfaMethod.fromString(row.getString(SqlMfaConfigurationTable.METHOD.getName()));
    OffsetDateTime createdAt = row.getOffsetDateTime(SqlMfaConfigurationTable.CREATED_AT.getName());
    UUID userId = row.getUUID(SqlMfaConfigurationTable.USER_ID.getName());

    if (method.equals(MfaMethod.TOTP)) {
      JsonObject params = (JsonObject) row.getValue(SqlMfaConfigurationTable.PARAMS.getName());
      return new MfaConfigurationDTO(params.getString("secret"), createdAt, userId);
    }

    return new SmsMfaSetup(createdAt, userId);
  }

  @Override
  public MfaConfiguration fromSql(Row row) {
    return SqlUserMfaDatasource.mapConfiguration(row);
  }

  @Override
  @Traced
  public CompletionStage<Collection<MfaConfiguration>> list(UUID userId) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(SqlMfaConfigurationTable.TABLE)
            .where(SqlMfaConfigurationTable.USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(this::findMany);
  }

  @Override
  @Traced
  public CompletionStage<Optional<MfaConfiguration>> retrieve(UUID userId, MfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(SqlMfaConfigurationTable.TABLE)
            .where(
                SqlMfaConfigurationTable.USER_ID
                    .eq(userId)
                    .and(SqlMfaConfigurationTable.METHOD.eq(method.getKey())));

    return sqlPool.execute(query).thenApply(this::findOne);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID userId, MfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(SqlMfaConfigurationTable.TABLE)
            .where(
                SqlMfaConfigurationTable.USER_ID
                    .eq(userId)
                    .and(SqlMfaConfigurationTable.METHOD.eq(method.getKey())))
            .returning(SqlMfaConfigurationTable.CREATED_AT);
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<MfaConfiguration> create(
      UUID userId, MfaMethod method, JsonObject params, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(SqlMfaConfigurationTable.TABLE)
            .columns(SqlMfaConfigurationTable.INSERT_FIELDS)
            .values(userId, params, method.getKey())
            .returning(SqlMfaConfigurationTable.CREATED_AT);

    return transaction
        .execute(query)
        .thenApply(rows -> rows.iterator().next())
        .thenApply(
            row -> {
              OffsetDateTime createdAt =
                  row.getOffsetDateTime(SqlMfaConfigurationTable.CREATED_AT.getName());
              if (method.equals(MfaMethod.TOTP)) {
                return new MfaConfigurationDTO(params.getString("secret"), createdAt, userId);
              }
              return new SmsMfaSetup(createdAt, userId);
            });
  }
}
