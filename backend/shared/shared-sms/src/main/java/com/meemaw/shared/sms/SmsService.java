package com.meemaw.shared.sms;

import java.util.concurrent.CompletionStage;

public interface SmsService {

  CompletionStage<SmsMessage> sendMessage(String from, String to, String body);
}
