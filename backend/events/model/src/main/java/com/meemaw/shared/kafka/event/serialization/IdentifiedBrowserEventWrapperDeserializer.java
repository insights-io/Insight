package com.meemaw.shared.kafka.event.serialization;

import com.meemaw.shared.event.model.IdentifiedBrowserEventWrapper;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class IdentifiedBrowserEventWrapperDeserializer extends
    ObjectMapperDeserializer<IdentifiedBrowserEventWrapper> {

  public IdentifiedBrowserEventWrapperDeserializer() {
    super(IdentifiedBrowserEventWrapper.class);
  }
}
