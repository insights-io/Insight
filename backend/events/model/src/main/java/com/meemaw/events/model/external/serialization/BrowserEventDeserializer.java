package com.meemaw.events.model.external.serialization;

import com.meemaw.events.model.external.BrowserEvent;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class BrowserEventDeserializer extends
    ObjectMapperDeserializer<BrowserEvent> {

  public BrowserEventDeserializer() {
    super(BrowserEvent.class);
  }
}
