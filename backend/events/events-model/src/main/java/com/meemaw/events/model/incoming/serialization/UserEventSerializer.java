package com.meemaw.events.model.incoming.serialization;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

import com.meemaw.events.model.incoming.UserEvent;

public class UserEventSerializer extends ObjectMapperSerializer<UserEvent> {}
