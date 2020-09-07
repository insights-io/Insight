package com.meemaw.auth.sso.tfa;

public class TfaChallengeValidatationException extends Exception {

  public TfaChallengeValidatationException(Exception exception) {
    super(exception);
  }
}
