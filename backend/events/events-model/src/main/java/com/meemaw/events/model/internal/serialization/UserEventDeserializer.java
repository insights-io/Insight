package com.meemaw.events.model.internal.serialization;

import com.meemaw.events.model.internal.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class UserEventDeserializer extends ObjectMapperDeserializer<UserEvent> {

  public UserEventDeserializer() {
    super(UserEvent.class);
  }
}
