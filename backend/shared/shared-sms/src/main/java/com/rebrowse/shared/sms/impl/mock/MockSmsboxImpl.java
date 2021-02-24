package com.rebrowse.shared.sms.impl.mock;

import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.shared.sms.SmsMessage;
import com.twilio.rest.api.v2010.account.Message.Status;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MockSmsboxImpl implements MockSmsbox {

  private final Map<String, List<SmsMessage>> sentMessages = new HashMap<>();
  private int sentMessagesCount;

  public CompletionStage<SmsMessage> send(String from, String to, String body) {
    SmsMessage message = new SmsMessage(from, to, body, OffsetDateTime.now(), Status.SENT);
    List<SmsMessage> messages =
        sentMessages.computeIfAbsent(message.getTo(), k -> new LinkedList<>());
    sentMessagesCount++;
    messages.add(message);
    return CompletableFuture.completedStage(message);
  }

  @Override
  public List<SmsMessage> getMessagesSentTo(String address) {
    return sentMessages.get(address);
  }

  @Override
  public void clear() {
    sentMessagesCount = 0;
    sentMessages.clear();
  }

  @Override
  public int getTotalMessagesSent() {
    return sentMessagesCount;
  }
}
