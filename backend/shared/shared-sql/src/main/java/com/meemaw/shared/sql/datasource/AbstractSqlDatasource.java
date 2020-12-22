package com.meemaw.shared.sql.datasource;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public abstract class AbstractSqlDatasource<T> {

  public abstract T fromSql(Row row);

  public Collection<T> findMany(RowSet<Row> rows) {
    ArrayList<T> users = new ArrayList<>(rows.size());
    rows.forEach(row -> users.add(fromSql(row)));
    return users;
  }

  public Optional<T> findOne(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(expectOne(rows));
  }

  public boolean hasNext(RowSet<Row> rows) {
    return rows.iterator().hasNext();
  }

  public T expectOne(RowSet<Row> rows) {
    return fromSql(rows.iterator().next());
  }
}
