package com.meemaw.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.concurrent.CompletionStage;
import org.jooq.DSLContext;
import org.jooq.Query;

public interface SqlPool {

  DSLContext getContext();

  CompletionStage<SqlTransaction> begin();

  CompletionStage<RowSet<Row>> query(Query query);

  CompletionStage<RowSet<Row>> query(Transaction transaction, Query query);
}
