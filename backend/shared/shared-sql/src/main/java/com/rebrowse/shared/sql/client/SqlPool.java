package com.rebrowse.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import java.util.concurrent.CompletionStage;
import org.jooq.DSLContext;
import org.jooq.Query;

public interface SqlPool extends SqlClient {

  DSLContext getContext();

  CompletionStage<SqlTransaction> beginTransaction();

  CompletionStage<RowSet<Row>> execute(Transaction transaction, Query query);
}
