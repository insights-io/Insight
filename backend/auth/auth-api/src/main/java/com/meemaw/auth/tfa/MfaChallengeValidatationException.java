package com.meemaw.auth.tfa;

public class MfaChallengeValidatationException extends Exception {

  public MfaChallengeValidatationException(Exception exception) {
    super(exception);
  }
}
