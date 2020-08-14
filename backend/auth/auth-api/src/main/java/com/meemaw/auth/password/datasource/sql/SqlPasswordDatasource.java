package com.meemaw.auth.password.datasource.sql;

import static com.meemaw.auth.password.datasource.sql.PasswordTable.HASH;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.TABLE;
import static com.meemaw.auth.password.datasource.sql.PasswordTable.USER_ID;

import com.meemaw.auth.password.datasource.PasswordDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;

@ApplicationScoped
@Slf4j
public class SqlPasswordDatasource implements PasswordDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Boolean> storePassword(
      UUID userId, String hashedPassword, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(USER_ID, HASH)
            .values(userId, hashedPassword);

    return transaction.query(query).thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Boolean> storePassword(UUID userId, String hashedPassword) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(USER_ID, HASH)
            .values(userId, hashedPassword);

    return sqlPool.execute(query).thenApply(pgRowSet -> true);
  }
}
