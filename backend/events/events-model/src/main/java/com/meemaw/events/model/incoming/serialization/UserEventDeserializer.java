package com.meemaw.events.model.incoming.serialization;

import com.meemaw.events.model.incoming.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class UserEventDeserializer extends ObjectMapperDeserializer<UserEvent> {

  public UserEventDeserializer() {
    super(UserEvent.class);
  }
}
