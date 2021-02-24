package com.rebrowse.shared.sql.client.pg;

import com.rebrowse.shared.sql.SQLContext;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.exception.SqlQueryException;
import com.rebrowse.shared.tracing.TracingUtils;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.pgclient.PgException;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class TracedPgPool implements SqlPool {

  @Inject Tracer tracer;
  @Inject PgPool pgPool;

  @Override
  public CompletionStage<SqlTransaction> beginTransaction() {
    log.debug("[SQL]: Starting transaction");
    return pgPool
        .begin()
        .map(transaction -> new SqlTransaction(transaction, this))
        .subscribeAsCompletionStage();
  }

  @Override
  public DSLContext getContext() {
    return SQLContext.POSTGRES;
  }

  @Override
  public CompletionStage<RowSet<Row>> execute(Query query) {
    return execute(pgPool, query, tracer, getContext());
  }

  @Override
  public CompletionStage<RowSet<Row>> execute(Transaction transaction, Query query) {
    return execute(transaction, query, tracer, getContext());
  }

  public static CompletionStage<RowSet<Row>> execute(
      SqlClient client, Query query, Tracer tracer, DSLContext context) {
    Span span = startExecuteSpan(tracer, context);

    String statement = query.getSQL(ParamType.NAMED);
    Tags.DB_STATEMENT.set(span, statement);

    List<Object> values = query.getBindValues();
    log.debug("[SQL]: Executing SQL statement={} values={}", statement, values);

    return client
        .preparedQuery(statement)
        .execute(Tuple.tuple(values))
        .subscribeAsCompletionStage()
        .thenApply(
            value -> {
              span.finish();
              return value;
            })
        .exceptionally(
            throwable -> {
              TracingUtils.finishExceptionally(span, throwable);
              log.error(
                  "[SQL]: Failed to execute SQL statement={} values={}",
                  statement,
                  values,
                  throwable);
              throw new SqlQueryException((PgException) throwable.getCause(), statement, values);
            });
  }

  private static Span startSqlSpan(String name, Tracer tracer, DSLContext context) {
    Span span = tracer.buildSpan(name).asChildOf(tracer.activeSpan()).start();
    Tags.DB_TYPE.set(span, context.dialect().getName());
    Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
    return span;
  }

  private static Span startExecuteSpan(Tracer tracer, DSLContext context) {
    return startSqlSpan("TracedPgPool.execute", tracer, context);
  }
}
