package com.meemaw.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.concurrent.CompletionStage;

public interface SqlPool extends SqlClient {

  DSLContext getContext();

  CompletionStage<SqlTransaction> beginTransaction();

  CompletionStage<RowSet<Row>> execute(Transaction transaction, Query query);
}
