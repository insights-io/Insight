package com.rebrowse.shared.sms;

import java.util.concurrent.CompletionStage;

public interface SmsService {

  CompletionStage<SmsMessage> sendMessage(String from, String to, String body);
}
