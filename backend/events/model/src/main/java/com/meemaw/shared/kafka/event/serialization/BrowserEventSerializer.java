package com.meemaw.shared.kafka.event.serialization;

import com.meemaw.shared.event.model.AbstractBrowserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class BrowserEventSerializer extends ObjectMapperSerializer<AbstractBrowserEvent> {

}
