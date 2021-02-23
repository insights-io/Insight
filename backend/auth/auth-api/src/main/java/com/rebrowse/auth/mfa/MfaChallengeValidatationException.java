package com.rebrowse.auth.mfa;

public class MfaChallengeValidatationException extends Exception {

  public MfaChallengeValidatationException(Exception exception) {
    super(exception);
  }
}
