package com.meemaw.shared.sql.client;

import com.meemaw.shared.sql.exception.SqlException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;

@Slf4j
public class SqlTransaction {

  private final Transaction transaction;
  private final SqlPool sqlPool;

  public SqlTransaction(Transaction transaction, SqlPool sqlPool) {
    this.transaction = Objects.requireNonNull(transaction);
    this.sqlPool = Objects.requireNonNull(sqlPool);
  }

  public Uni<RowSet<Row>> query(Query query) {
    return sqlPool.query(transaction, query);
  }

  public Uni<Void> commit() {
    log.debug("[SQL]: Committing transaction");
    return transaction
        .commit()
        .onFailure()
        .apply(
            throwable -> {
              log.error("[SQL]: Failed to commit transaction", throwable);
              throw new SqlException(throwable);
            });
  }
}
