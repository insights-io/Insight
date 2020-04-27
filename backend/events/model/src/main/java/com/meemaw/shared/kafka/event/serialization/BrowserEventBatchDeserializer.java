package com.meemaw.shared.kafka.event.serialization;

import com.meemaw.shared.event.model.AbstractBrowserEventBatch;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class BrowserEventBatchDeserializer extends ObjectMapperDeserializer<AbstractBrowserEventBatch> {

  public BrowserEventBatchDeserializer() {
    super(AbstractBrowserEventBatch.class);
  }
}
