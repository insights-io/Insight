package com.meemaw.events.model.outgoing.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.events.model.incoming.BrowserEventTypeConstants;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventDtoSerializationTest {

  @Test
  public void loadEventDtoDeserialization() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 8, \"location\": \"http://localhost:8080\"}";
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserLoadEventDTO.class, deserialized.getClass());

    BrowserLoadEventDTO browserUnloadEvent = (BrowserLoadEventDTO) deserialized;
    assertEquals("http://localhost:8080", browserUnloadEvent.getLocation());
    assertEquals(1234, browserUnloadEvent.getTimestamp());
    assertEquals(BrowserEventTypeConstants.LOAD, browserUnloadEvent.getEventType());
  }
}
