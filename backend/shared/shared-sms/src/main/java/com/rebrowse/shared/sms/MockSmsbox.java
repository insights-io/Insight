package com.rebrowse.shared.sms;

import java.util.List;

public interface MockSmsbox {

  List<SmsMessage> getMessagesSentTo(String address);

  void clear();

  int getTotalMessagesSent();
}
