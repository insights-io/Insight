package com.rebrowse.shared.sql.client;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.concurrent.CompletionStage;
import org.jooq.Query;

public interface SqlClient {

  CompletionStage<RowSet<Row>> execute(Query query);
}
