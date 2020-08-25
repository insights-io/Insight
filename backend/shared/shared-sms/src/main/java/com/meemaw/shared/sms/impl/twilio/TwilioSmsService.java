package com.meemaw.shared.sms.impl.twilio;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.meemaw.shared.sms.SmsMessage;
import com.meemaw.shared.sms.SmsService;
import com.meemaw.shared.sms.impl.mock.MockSmsboxImpl;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import io.quarkus.runtime.StartupEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
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
      log.info("[SMS]: Sending message from={} to={} body={}", from, to, body);
      return mockSmsbox.send(from, to, body);
    }

    CompletableFuture<SmsMessage> future = new CompletableFuture<>();
    MessageCreator messageCreator =
        Message.creator(new PhoneNumber(to), new PhoneNumber(from), body);

    Futures.addCallback(
        messageCreator.createAsync(),
        new FutureCallback<>() {
          @Override
          public void onSuccess(@Nullable Message result) {
            if (result == null) {
              this.onFailure(new FailedDeliveryException("Null result"));
              return;
            }

            log.info("[SMS]: message delivery result={}", result);
            SmsMessage message =
                new SmsMessage(from, to, body, result.getDateSent(), result.getStatus());
            future.complete(message);
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("[SMS]: Failed to send message from={} to={} body={}", from, to, body, ex);
            future.completeExceptionally(ex);
          }
        },
        Twilio.getExecutorService());

    return future;
  }
}
