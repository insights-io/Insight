package com.meemaw.events.model.internal.serialization;

import com.meemaw.events.model.internal.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class UserEventSerializer extends ObjectMapperSerializer<UserEvent> {}
