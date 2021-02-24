package com.rebrowse.shared.sql.exception;

import io.vertx.pgclient.PgException;
import java.util.List;

public class SqlQueryException extends SqlException {

  private final String statement;
  private final List<Object> values;

  public SqlQueryException(PgException pgException, String statement, List<Object> values) {
    super(pgException);
    this.statement = statement;
    this.values = values;
  }

  public String getStatement() {
    return statement;
  }

  public List<Object> getValues() {
    return values;
  }
}
