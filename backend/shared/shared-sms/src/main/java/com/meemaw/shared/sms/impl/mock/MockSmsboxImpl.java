package com.meemaw.shared.sms.impl.mock;

import com.meemaw.shared.sms.MockSmsbox;
import com.meemaw.shared.sms.SmsMessage;
import com.twilio.rest.api.v2010.account.Message.Status;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import org.joda.time.DateTime;

@ApplicationScoped
public class MockSmsboxImpl implements MockSmsbox {

  private final Map<String, List<SmsMessage>> sentMessages = new HashMap<>();
  private int sentMessagesCount;

  public CompletionStage<SmsMessage> send(String from, String to, String body) {
    SmsMessage message = new SmsMessage(from, to, body, new DateTime(), Status.SENT);
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
