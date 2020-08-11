package com.meemaw.auth.user.datasource.sql;

import static com.meemaw.auth.user.datasource.sql.TFASetupTable.CREATED_AT;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.SECRET;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.TABLE;
import static com.meemaw.auth.user.datasource.sql.TFASetupTable.USER_ID;

import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.TfaSetup;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlUserTfaDatasource implements UserTfaDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<Optional<TfaSetup>> get(UUID userId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(USER_ID.eq(userId));
    return sqlPool
        .execute(query)
        .thenApply(
            rows -> {
              if (!rows.iterator().hasNext()) {
                return Optional.empty();
              }

              Row row = rows.iterator().next();
              return Optional.of(
                  new TfaSetup(
                      row.getString(SECRET.getName()),
                      row.getOffsetDateTime(CREATED_AT.getName())));
            });
  }

  @Override
  public CompletionStage<TfaSetup> store(UUID userId, String secret, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(TFASetupTable.INSERT_FIELDS)
            .values(userId, secret)
            .returning(CREATED_AT);

    return transaction
        .query(query)
        .thenApply(
            rows ->
                new TfaSetup(
                    secret, rows.iterator().next().getOffsetDateTime(CREATED_AT.getName())));
  }
}
