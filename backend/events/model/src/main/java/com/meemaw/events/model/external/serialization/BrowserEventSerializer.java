package com.meemaw.events.model.external.serialization;

import com.meemaw.events.model.external.BrowserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class BrowserEventSerializer extends
    ObjectMapperSerializer<BrowserEvent> {
}
