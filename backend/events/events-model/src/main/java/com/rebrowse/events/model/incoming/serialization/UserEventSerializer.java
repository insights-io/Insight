package com.rebrowse.events.model.incoming.serialization;

import com.rebrowse.events.model.incoming.UserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class UserEventSerializer extends ObjectMapperSerializer<UserEvent> {}
