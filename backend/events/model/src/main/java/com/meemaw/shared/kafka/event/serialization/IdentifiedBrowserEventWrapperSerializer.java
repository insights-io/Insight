package com.meemaw.shared.kafka.event.serialization;

import com.meemaw.shared.event.model.IdentifiedBrowserEventWrapper;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class IdentifiedBrowserEventWrapperSerializer extends
    ObjectMapperSerializer<IdentifiedBrowserEventWrapper> {
}
