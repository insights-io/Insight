package com.meemaw.auth.tfa;

public class TfaChallengeValidatationException extends Exception {

  public TfaChallengeValidatationException(Exception exception) {
    super(exception);
  }
}
