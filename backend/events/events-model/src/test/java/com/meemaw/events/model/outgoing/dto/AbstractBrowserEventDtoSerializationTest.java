package com.meemaw.events.model.outgoing.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.events.model.shared.BrowserEventType;
import com.meemaw.events.model.shared.LogLevel;
import com.meemaw.test.matchers.SameJSON;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventDtoSerializationTest {

  @Test
  public void loadEventDtoDeserializationTest() throws JsonProcessingException, JSONException {
    String payload = "{\"t\": 1234, \"e\": 8, \"location\": \"http://localhost:8080\"}";
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserLoadEventDTO.class, deserialized.getClass());

    BrowserLoadEventDTO event = (BrowserLoadEventDTO) deserialized;
    assertEquals("http://localhost:8080", event.getLocation());
    assertEquals(1234, event.getTimestamp());
    assertEquals(BrowserEventType.LOAD, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void logEventDtoSerializationTest() throws JsonProcessingException, JSONException {
    String payload = "{\"t\": 1234, \"e\": 9, \"level\": \"error\", \"arguments\": []}";
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserLogEventDTO.class, deserialized.getClass());

    BrowserLogEventDTO event = (BrowserLogEventDTO) deserialized;
    assertEquals(LogLevel.ERROR, event.getLevel());
    assertEquals(BrowserEventType.LOG, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void errorEventDtoSerializationTest() throws JsonProcessingException, JSONException {
    String payload =
        "{\"t\":10158.850000007078,\"e\":10,\"a\":[\"Unexpected identifier\",\"SyntaxError\",\"SyntaxError: Unexpected identifier\n"
            + "    at <anonymous>:1:1\n"
            + "    at eval (__playwright_evaluation_script__45:11:47)\n"
            + "    at UtilityScript.callFunction (__playwright_evaluation_script__1:299:24)\n"
            + "    at UtilityScript.<anonymous> (__playwright_evaluation_script__46:1:44)\"]}";

    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserErrorEventDTO.class, deserialized.getClass());

    BrowserLogEventDTO event = (BrowserLogEventDTO) deserialized;
    assertEquals(LogLevel.ERROR, event.getLevel());
    assertEquals(BrowserEventType.LOG, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }
}
