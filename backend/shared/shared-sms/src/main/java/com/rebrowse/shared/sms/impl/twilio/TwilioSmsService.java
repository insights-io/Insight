package com.rebrowse.shared.sms.impl.twilio;

import com.rebrowse.shared.sms.SmsMessage;
import com.rebrowse.shared.sms.SmsService;
import com.rebrowse.shared.sms.impl.mock.MockSmsboxImpl;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.quarkus.runtime.StartupEvent;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
@ApplicationScoped
public class TwilioSmsService implements SmsService {

  @ConfigProperty(name = "sms.client.username")
  String accountSid;

  @ConfigProperty(name = "sms.client.password")
  String authToken;

  @ConfigProperty(name = "sms.client.mock")
  boolean clientMock;

  @Inject MockSmsboxImpl mockSmsbox;

  public void init(@Observes StartupEvent event) {
    log.info("[SMS]: Initializing TwilioSmsService for account={}", accountSid);
    Twilio.init(accountSid, authToken);
  }

  @Override
  @Traced
  public CompletionStage<SmsMessage> sendMessage(String from, String to, String body) {
    if (clientMock) {
      log.info("[AUTH]: Sending SMS message from={} to={} body={}", from, to, body);
      return mockSmsbox.send(from, to, body);
    }

    return Message.creator(new PhoneNumber(to), new PhoneNumber(from), body)
        .createAsync()
        .thenApply(
            message -> {
              log.info("[AUTH]: SMS delivery successful message={}", message);
              OffsetDateTime dateSent = message.getDateSent().toOffsetDateTime();
              return new SmsMessage(from, to, body, dateSent, message.getStatus());
            })
        .exceptionallyAsync(
            ex -> {
              log.error(
                  "[AUTH]: Failed to send SMS message from={} to={} body={}", from, to, body, ex);
              throw new CompletionException(ex);
            });
  }
}
