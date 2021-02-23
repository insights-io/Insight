package com.rebrowse.shared.rest.exception;

import java.util.Map;

public class SortBySearchParseException extends Exception {

  private final Map<String, String> errors;

  public SortBySearchParseException(Map<String, String> errors) {
    super();
    this.errors = errors;
  }

  public Map<String, String> getErrors() {
    return errors;
  }
}
