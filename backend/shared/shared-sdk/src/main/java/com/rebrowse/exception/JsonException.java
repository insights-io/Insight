package com.rebrowse.exception;

public class JsonException extends RebrowseException {

  public JsonException(Throwable throwable) {
    super(throwable.getMessage(), null, 0, throwable);
  }
}
