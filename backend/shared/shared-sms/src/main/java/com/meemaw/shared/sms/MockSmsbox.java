package com.meemaw.shared.sms;

import java.util.List;

public interface MockSmsbox {

  List<SmsMessage> getMessagesSentTo(String address);

  void clear();

  int getTotalMessagesSent();
}
