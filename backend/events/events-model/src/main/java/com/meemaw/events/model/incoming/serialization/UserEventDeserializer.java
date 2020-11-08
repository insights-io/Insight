package com.meemaw.events.model.incoming.serialization;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

import com.meemaw.events.model.incoming.UserEvent;

public class UserEventDeserializer extends ObjectMapperDeserializer<UserEvent> {

  public UserEventDeserializer() {
    super(UserEvent.class);
  }
}
