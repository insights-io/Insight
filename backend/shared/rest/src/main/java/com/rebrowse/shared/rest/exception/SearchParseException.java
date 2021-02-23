package com.rebrowse.shared.rest.exception;

import java.util.Map;

public class SearchParseException extends RuntimeException {

  private final Map<String, Object> errors;

  public SearchParseException(Map<String, Object> errors) {
    super();
    this.errors = errors;
  }

  public Map<String, Object> getErrors() {
    return errors;
  }
}
