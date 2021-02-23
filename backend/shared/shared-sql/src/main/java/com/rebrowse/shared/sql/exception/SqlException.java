package com.rebrowse.shared.sql.exception;

import com.rebrowse.shared.rest.exception.DatasourceException;
import io.vertx.pgclient.PgException;

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
