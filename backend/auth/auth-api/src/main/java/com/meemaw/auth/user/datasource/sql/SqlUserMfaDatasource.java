package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.user.datasource.sql.SqlMfaConfigurationTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.SqlMfaConfigurationTable.METHOD;
import static com.meemaw.auth.user.datasource.sql.SqlMfaConfigurationTable.PARAMS;
import static com.meemaw.auth.user.datasource.sql.SqlMfaConfigurationTable.TABLE;
import static com.meemaw.auth.user.datasource.sql.SqlMfaConfigurationTable.USER_ID;

import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.model.MfaConfiguration;
import com.meemaw.auth.mfa.sms.model.SmsMfaSetup;
import com.meemaw.auth.mfa.totp.model.MfaConfigurationDTO;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
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
public class SqlUserMfaDatasource implements UserMfaDatasource {

  @Inject SqlPool sqlPool;

  public static Optional<MfaConfiguration> maybeMapMfaConfiguration(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }

    Row row = rows.iterator().next();
    return Optional.of(mapConfiguration(row));
  }

  public static MfaConfiguration mapConfiguration(Row row) {
    MfaMethod method = MfaMethod.fromString(row.getString(METHOD.getName()));
    OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
    UUID userId = row.getUUID(USER_ID.getName());

    if (method.equals(MfaMethod.TOTP)) {
      JsonObject params = (JsonObject) row.getValue(PARAMS.getName());
      return new MfaConfigurationDTO(params.getString("secret"), createdAt, userId);
    }

    return new SmsMfaSetup(createdAt, userId);
  }

  public static List<MfaConfiguration> mapConfigurations(RowSet<Row> rows) {
    List<MfaConfiguration> mfaConfigurations = new ArrayList<>(rows.size());
    for (Row row : rows) {
      mfaConfigurations.add(mapConfiguration(row));
    }
    return mfaConfigurations;
  }

  @Override
  @Traced
  public CompletionStage<List<MfaConfiguration>> list(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(USER_ID.eq(userId));
    return sqlPool.execute(query).thenApply(SqlUserMfaDatasource::mapConfigurations);
  }

  @Override
  @Traced
  public CompletionStage<Optional<MfaConfiguration>> retrieve(UUID userId, MfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(TABLE)
            .where(USER_ID.eq(userId).and(METHOD.eq(method.getKey())));
    return sqlPool.execute(query).thenApply(SqlUserMfaDatasource::maybeMapMfaConfiguration);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID userId, MfaMethod method) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(USER_ID.eq(userId).and(METHOD.eq(method.getKey())))
            .returning(CREATED_AT);
    return sqlPool.execute(query).thenApply(rows -> rows.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<MfaConfiguration> create(
      UUID userId, MfaMethod method, JsonObject params, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(SqlMfaConfigurationTable.INSERT_FIELDS)
            .values(userId, params, method.getKey())
            .returning(CREATED_AT);

    return transaction
        .execute(query)
        .thenApply(rows -> rows.iterator().next())
        .thenApply(
            row -> {
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              if (method.equals(MfaMethod.TOTP)) {
                return new MfaConfigurationDTO(params.getString("secret"), createdAt, userId);
              }
              return new SmsMfaSetup(createdAt, userId);
            });
  }
}
