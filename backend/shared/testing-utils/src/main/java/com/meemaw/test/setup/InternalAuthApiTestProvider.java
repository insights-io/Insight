package com.meemaw.test.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import java.util.List;

public class InternalAuthApiTestProvider extends AuthApiTestProvider {

  public InternalAuthApiTestProvider(ObjectMapper objectMapper, MockMailbox mailbox) {
    super(
        null,
        objectMapper,
        (email) -> {
          List<Mail> messages = mailbox.getMessagesSentTo(email);
          Mail lastMessage = messages.get(messages.size() - 1);
          return EmailTestUtils.parseLink(lastMessage);
        });
  }
}
