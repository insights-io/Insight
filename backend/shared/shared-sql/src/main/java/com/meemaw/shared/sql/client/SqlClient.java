package com.meemaw.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import org.jooq.Query;

import java.util.concurrent.CompletionStage;

public interface SqlClient {

  CompletionStage<RowSet<Row>> execute(Query query);
}
