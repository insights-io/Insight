package com.meemaw.shared.sms;

import com.twilio.rest.api.v2010.account.Message;
import lombok.Value;
import org.joda.time.DateTime;

@Value
public class SmsMessage {

  String from;
  String to;
  String body;
  DateTime dateSent;
  Message.Status status;
}
