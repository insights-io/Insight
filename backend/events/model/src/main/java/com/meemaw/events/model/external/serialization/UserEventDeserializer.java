package com.meemaw.events.model.external.serialization;

import com.meemaw.events.model.external.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class UserEventDeserializer extends ObjectMapperDeserializer<UserEvent> {

  public UserEventDeserializer() {
    super(UserEvent.class);
  }
}
