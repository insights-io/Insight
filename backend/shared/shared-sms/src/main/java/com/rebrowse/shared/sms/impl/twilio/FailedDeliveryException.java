package com.rebrowse.shared.sms.impl.twilio;

public class FailedDeliveryException extends Exception {

  public FailedDeliveryException(String message) {
    super(message);
  }
}
