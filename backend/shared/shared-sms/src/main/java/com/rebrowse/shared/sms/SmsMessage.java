package com.rebrowse.shared.sms;

import com.twilio.rest.api.v2010.account.Message;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SmsMessage {

  String from;
  String to;
  String body;
  OffsetDateTime dateSent;
  Message.Status status;
}
