package com.meemaw.shared.sql.client;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Transaction;
import org.jooq.DSLContext;
import org.jooq.Query;

public interface SqlPool {

  DSLContext getContext();

  Uni<SqlTransaction> begin();

  Uni<RowSet<Row>> query(Query query);

  Uni<RowSet<Row>> query(Transaction transaction, Query query);
}
