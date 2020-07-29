package com.meemaw.shared.sql.client.pg;

import com.meemaw.shared.sql.SQLContext;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.exception.SqlQueryException;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Transaction;
import io.vertx.mutiny.sqlclient.Tuple;
import java.util.List;
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
  public Uni<SqlTransaction> begin() {
    log.debug("[SQL]: Starting transaction");
    return pgPool.begin().map(transaction -> new SqlTransaction(transaction, this));
  }

  @Override
  public DSLContext getContext() {
    return SQLContext.POSTGRES;
  }

  @Override
  public Uni<RowSet<Row>> query(Query query) {
    return query(pgPool, query, tracer, getContext());
  }

  @Override
  public Uni<RowSet<Row>> query(Transaction transaction, Query query) {
    return query(transaction, query, tracer, getContext());
  }

  public static Uni<RowSet<Row>> query(
      SqlClient client, Query query, Tracer tracer, DSLContext context) {
    Span span = startQuerySpan(tracer, context);

    String statement = query.getSQL(ParamType.NAMED);
    Tags.DB_STATEMENT.set(span, statement);

    List<Object> values = query.getBindValues();
    log.debug("[SQL]: Executing SQL statement: {} values: {}", statement, values);

    return client
        .preparedQuery(statement)
        .execute(Tuple.tuple(values))
        .map(
            value -> {
              span.finish();
              return value;
            })
        .onFailure()
        .apply(
            throwable -> {
              Tags.ERROR.set(span, true);
              span.log(throwable.getMessage());
              span.finish();
              log.error("[SQL]: Failed to execute SQL statement: {}", statement, throwable);
              throw new SqlQueryException(throwable, statement, values);
            });
  }

  private static Span startQuerySpan(Tracer tracer, DSLContext context) {
    Span span = tracer.buildSpan("TracedPgPool.query").start();
    Tags.DB_TYPE.set(span, context.dialect().getName());
    Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
    return span;
  }
}
