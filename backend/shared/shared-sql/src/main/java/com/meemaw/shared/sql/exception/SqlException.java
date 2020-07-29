package com.meemaw.shared.sql.exception;

public class SqlException extends RuntimeException {

  public SqlException(Throwable throwable) {
    super(throwable);
  }
}
