package com.rebrowse.events.model.outgoing.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rebrowse.events.model.shared.BrowserEventType;
import com.rebrowse.events.model.shared.LogLevel;
import com.rebrowse.test.matchers.SameJSON;
import com.rebrowse.test.rest.data.EventTestData;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventDtoSerializationTest {

  @Test
  public void __0__navigateEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("0__navigate.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserNavigateEventDTO.class, deserialized.getClass());

    BrowserNavigateEventDTO event = (BrowserNavigateEventDTO) deserialized;
    assertEquals(1234, event.getTimestamp());
    assertEquals("http://localhost:8080", event.getLocation());
    assertEquals("Test title", event.getTitle());
    assertEquals(BrowserEventType.NAVIGATE, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __1__unloadEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("1__unload.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserUnloadEventDTO.class, deserialized.getClass());

    BrowserUnloadEventDTO event = (BrowserUnloadEventDTO) deserialized;
    assertEquals(1234, event.getTimestamp());
    assertEquals("http://localhost:8080", event.getLocation());
    assertEquals(BrowserEventType.UNLOAD, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __2__resizeEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("2__resize.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserResizeEventDTO.class, deserialized.getClass());

    BrowserResizeEventDTO event = (BrowserResizeEventDTO) deserialized;
    assertEquals(1234, event.getTimestamp());
    assertEquals(551, event.getInnerWidth());
    assertEquals(232, event.getInnerHeight());
    assertEquals(1234, event.getTimestamp());
    assertEquals(BrowserEventType.RESIZE, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __3__performanceEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("3__performance.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserPerformanceEventDTO.class, deserialized.getClass());

    BrowserPerformanceEventDTO event = (BrowserPerformanceEventDTO) deserialized;
    assertEquals(17, event.getTimestamp());
    assertEquals("http://localhost:3002/", event.getName());
    assertEquals("navigation", event.getEntryType());
    assertEquals(0, event.getStartTime());
    assertEquals(5478.304999996908, event.getDuration());
    assertEquals(BrowserEventType.PERFORMANCE, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __4__clickEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("4__click.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserClickEventDTO.class, deserialized.getClass());

    BrowserClickEventDTO event = (BrowserClickEventDTO) deserialized;
    assertEquals(1306, event.getTimestamp());
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(
        Map.of(
            "type",
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNode());
    assertEquals(BrowserEventType.CLICK, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __5__mousemoveEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("5__mousemove.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserMouseMoveEventDTO.class, deserialized.getClass());

    BrowserMouseMoveEventDTO event = (BrowserMouseMoveEventDTO) deserialized;
    assertEquals(1306, event.getTimestamp());
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(
        Map.of(
            "type",
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNode());
    assertEquals(BrowserEventType.MOUSEMOVE, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __6__mousemoveEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("6__mousedown.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserMouseDownEventDTO.class, deserialized.getClass());

    BrowserMouseDownEventDTO event = (BrowserMouseDownEventDTO) deserialized;
    assertEquals(1306, event.getTimestamp());
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(
        Map.of(
            "type",
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNode());
    assertEquals(BrowserEventType.MOUSEDOWN, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __7__mouseupEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("7__mouseup.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserMouseUpEventDTO.class, deserialized.getClass());

    BrowserMouseUpEventDTO event = (BrowserMouseUpEventDTO) deserialized;
    assertEquals(1306, event.getTimestamp());
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(
        Map.of(
            "type",
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNode());
    assertEquals(BrowserEventType.MOUSEUP, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __8__loadEventDtoDeserializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("8__load.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserLoadEventDTO.class, deserialized.getClass());

    BrowserLoadEventDTO event = (BrowserLoadEventDTO) deserialized;
    assertEquals(1234, event.getTimestamp());
    assertEquals("http://localhost:8080", event.getLocation());
    assertEquals(BrowserEventType.LOAD, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __9__logEventDtoSerializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("9__log.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserLogEventDTO.class, deserialized.getClass());

    BrowserLogEventDTO event = (BrowserLogEventDTO) deserialized;
    assertEquals(1234, event.getTimestamp());
    assertEquals(LogLevel.ERROR, event.getLevel());
    assertEquals(BrowserEventType.LOG, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __10__errorEventDtoSerializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("10__error.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserErrorEventDTO.class, deserialized.getClass());

    BrowserErrorEventDTO event = (BrowserErrorEventDTO) deserialized;
    assertEquals(10812, event.getTimestamp());
    assertEquals("simulated error", event.getMessage());
    assertEquals("Error", event.getName());
    assertEquals(
        "Error: simulated error\n    at <anonymous>:1:7\n    at eval (__playwright_evaluation_script__45:7:47)\n    at UtilityScript.callFunction (__playwright_evaluation_script__1:299:24)\n    at UtilityScript.<anonymous> (__playwright_evaluation_script__46:1:44)",
        event.getStack());
    assertEquals(BrowserEventType.ERROR, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __11__xhrEventDtoSerializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("11__xhr.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserXhrEventDTO.class, deserialized.getClass());

    BrowserXhrEventDTO event = (BrowserXhrEventDTO) deserialized;
    assertEquals(10812, event.getTimestamp());
    assertEquals("GET", event.getMethod());
    assertEquals("http://localhost:8082/v1/sessions", event.getUrl());
    assertEquals(200, event.getStatus());
    assertEquals("cors", event.getType());
    assertNull(event.getInitiatorType());
    assertNull(event.getNextHopProtocol());
    assertEquals(BrowserEventType.XHR, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __11__xhrResourcePerformanceEnrichedEventDtoSerializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("11__xhr__resourceperformance_enriched.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserXhrEventDTO.class, deserialized.getClass());

    BrowserXhrEventDTO event = (BrowserXhrEventDTO) deserialized;
    assertEquals(10812, event.getTimestamp());
    assertEquals("GET", event.getMethod());
    assertEquals("http://localhost:8082/v1/sessions", event.getUrl());
    assertEquals(200, event.getStatus());
    assertEquals("cors", event.getType());
    assertEquals("fetch", event.getInitiatorType());
    assertEquals("http/1.1", event.getNextHopProtocol());
    assertEquals(BrowserEventType.XHR, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }

  @Test
  public void __12__resourceperformanceEventDtoSerializationTest()
      throws IOException, JSONException, URISyntaxException {
    String payload = EventTestData.readOutgoingEvent("12__resourceperformance.json");
    AbstractBrowserEventDTO deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEventDTO.class);
    assertEquals(BrowserResourcePerformanceEventDTO.class, deserialized.getClass());

    BrowserResourcePerformanceEventDTO event = (BrowserResourcePerformanceEventDTO) deserialized;
    assertEquals(10812, event.getTimestamp());
    assertEquals("http://localhost:8082/v1/sessions", event.getName());
    assertEquals(20, event.getStartTime());
    assertEquals(40, event.getDuration());
    assertEquals("fetch", event.getInitiatorType());
    assertEquals("http/1.1", event.getNextHopProtocol());
    assertEquals(BrowserEventType.RESOURCE_PERFORMANCE, event.getEventType());
    SameJSON.assertEquals(payload, JacksonMapper.get().writeValueAsString(event));
  }
}
