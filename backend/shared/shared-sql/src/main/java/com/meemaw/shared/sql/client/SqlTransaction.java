package com.meemaw.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
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

  public CompletionStage<RowSet<Row>> query(Query query) {
    return sqlPool.query(transaction, query);
  }

  public CompletionStage<Void> rollback() {
    log.debug("[SQL]: Rolling back transaction");
    return transaction
        .rollback()
        .subscribeAsCompletionStage()
        .exceptionally(
            throwable -> {
              log.error("[SQL]: Failed to rollback transaction");
              throw (CompletionException) throwable;
            });
  }

  public CompletionStage<Void> commit() {
    log.debug("[SQL]: Committing transaction");
    return transaction
        .commit()
        .subscribeAsCompletionStage()
        .exceptionally(
            throwable -> {
              log.error("[SQL]: Failed to commit transaction", throwable);
              throw (CompletionException) throwable;
            });
  }
}
