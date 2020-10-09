package com.rebrowse.exception;

public class AuthenticationException extends RebrowseException {

  public AuthenticationException(String message) {
    super(message, null, 0, null);
  }
}
