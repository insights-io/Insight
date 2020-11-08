package com.meemaw.events.model.incoming.serialization;

import com.meemaw.events.model.incoming.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class UserEventSerializer extends ObjectMapperSerializer<UserEvent> {}
