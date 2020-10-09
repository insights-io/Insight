package com.rebrowse.exception;

import lombok.Getter;

@Getter
public abstract class RebrowseException extends RuntimeException {

  private final String requestId;
  private final int statusCode;

  public RebrowseException(String message, String requestId, int statusCode) {
    this(message, requestId, statusCode, null);
  }

  public RebrowseException(String message, String requestId, int statusCode, Throwable throwable) {
    super(message, throwable);
    this.requestId = requestId;
    this.statusCode = statusCode;
  }
}
