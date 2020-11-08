package com.meemaw.shared.sql.exception;

import io.vertx.pgclient.PgException;

import com.meemaw.shared.rest.exception.DatasourceException;

public class SqlException extends DatasourceException {

  private final PgException pgException;

  public SqlException(PgException pgException) {
    super(pgException);
    this.pgException = pgException;
  }

  public String getSeverity() {
    return pgException.getSeverity();
  }

  public String getCode() {
    return pgException.getCode();
  }

  public String getDetail() {
    return pgException.getDetail();
  }

  public boolean hadConflict() {
    return pgException.getCode().equals(SqlErrorCode.UNIQUE_VIOLATION.getCode());
  }
}
