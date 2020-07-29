package com.meemaw.shared.sql.exception;

import java.util.List;

public class SqlQueryException extends SqlException {

  private final String statement;
  private final List<Object> values;

  public SqlQueryException(Throwable throwable, String statement, List<Object> values) {
    super(throwable);
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
