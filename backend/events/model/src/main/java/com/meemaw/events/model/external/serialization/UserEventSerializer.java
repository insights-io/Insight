package com.meemaw.events.model.external.serialization;

import com.meemaw.events.model.external.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class UserEventSerializer extends ObjectMapperSerializer<UserEvent> {}
