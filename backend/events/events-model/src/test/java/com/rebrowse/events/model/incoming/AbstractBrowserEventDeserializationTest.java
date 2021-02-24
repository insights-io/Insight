package com.rebrowse.events.model.incoming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rebrowse.events.model.shared.BrowserEventType;
import com.rebrowse.events.model.shared.LogLevel;
import com.rebrowse.test.rest.data.EventTestData;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventDeserializationTest {

  @Test
  public void __0__navigateBeaconEventDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("0__navigate.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserNavigateEvent.class, deserialized.getClass());

    BrowserNavigateEvent event = (BrowserNavigateEvent) deserialized;
    Assertions.assertEquals("http://localhost:8080", event.arguments.getLocation());
    Assertions.assertEquals("Test title", event.arguments.getTitle());
    Assertions.assertEquals(BrowserEventType.NAVIGATE, event.getEventType());
  }

  @Test
  public void __1__unloadBeaconEventDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("1__unload.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserUnloadEvent.class, deserialized.getClass());

    BrowserUnloadEvent event = (BrowserUnloadEvent) deserialized;
    Assertions.assertEquals("http://localhost:8080", event.arguments.getLocation());
    Assertions.assertEquals(BrowserEventType.UNLOAD, event.getEventType());
  }

  @Test
  public void __2__resizeBeaconEventDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("2__resize.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserResizeEvent.class, deserialized.getClass());

    BrowserResizeEvent event = (BrowserResizeEvent) deserialized;
    Assertions.assertEquals(551, event.arguments.getInnerWidth());
    Assertions.assertEquals(232, event.arguments.getInnerHeight());
    Assertions.assertEquals(BrowserEventType.RESIZE, event.getEventType());
  }

  @Test
  public void __3__performanceNavigationBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("3__performance.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserPerformanceEvent.class, deserialized.getClass());

    BrowserPerformanceEvent event = (BrowserPerformanceEvent) deserialized;
    Assertions.assertEquals("http://localhost:3002/", event.arguments.getName());
    Assertions.assertEquals("navigation", event.arguments.getEntryType());
    Assertions.assertEquals(0, event.arguments.getStartTime());
    Assertions.assertEquals(5478.304999996908, event.arguments.getDuration());
    Assertions.assertEquals(BrowserEventType.PERFORMANCE, event.getEventType());
  }

  @Test
  public void __4__clickNodeBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("4__click.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserClickEvent.class, deserialized.getClass());

    BrowserClickEvent event = (BrowserClickEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    Assertions.assertEquals(BrowserEventType.CLICK, event.getEventType());
  }

  @Test
  public void __5__mouseMoveBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("5__mousemove.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserMouseMoveEvent.class, deserialized.getClass());

    BrowserMouseMoveEvent event = (BrowserMouseMoveEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    Assertions.assertEquals(BrowserEventType.MOUSEMOVE, event.getEventType());
  }

  @Test
  public void __6__mouseDownBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("6__mousedown.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserMouseDownEvent.class, deserialized.getClass());

    BrowserMouseDownEvent event = (BrowserMouseDownEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    Assertions.assertEquals(BrowserEventType.MOUSEDOWN, event.getEventType());
  }

  @Test
  public void __7__mouseUpBeaconEventDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("7__mouseup.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserMouseUpEvent.class, deserialized.getClass());

    BrowserMouseUpEvent event = (BrowserMouseUpEvent) deserialized;
    assertEquals(1167, event.getClientX());
    assertEquals(732, event.getClientY());
    assertEquals(Optional.of("BUTTON"), event.getNode());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        event.getNodeWithAttributes());
    Assertions.assertEquals(BrowserEventType.MOUSEUP, event.getEventType());
  }

  @Test
  public void __8__loadBeaconEventDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("8__load.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserLoadEvent.class, deserialized.getClass());

    BrowserLoadEvent event = (BrowserLoadEvent) deserialized;
    Assertions.assertEquals("http://localhost:8080", event.arguments.getLocation());
    Assertions.assertEquals(BrowserEventType.LOAD, event.getEventType());
  }

  @Test
  public void __9__logEventBeaconDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("9__log.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserLogEvent.class, deserialized.getClass());

    BrowserLogEvent event = (BrowserLogEvent) deserialized;
    Assertions.assertEquals(LogLevel.ERROR, event.getLevel());
    assertEquals(List.of("HAHA"), event.getArguments());
    Assertions.assertEquals(BrowserEventType.LOG, event.getEventType());
  }

  @Test
  public void __10__errorEventBeaconDeserializationTest() throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("10__error.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserErrorEvent.class, deserialized.getClass());

    BrowserErrorEvent event = (BrowserErrorEvent) deserialized;
    Assertions.assertEquals("simulated error", event.getArguments().getMessage());
    Assertions.assertEquals("Error", event.getArguments().getName());
    Assertions.assertTrue(event.getArguments().getStack().contains("Error: simulated error"));
    Assertions.assertEquals(BrowserEventType.ERROR, event.getEventType());
  }

  @Test
  public void __11__xhr_xmlhttprequestBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("11__xhr__xmlhttprequest.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserXhrEvent.class, deserialized.getClass());

    BrowserXhrEvent event = (BrowserXhrEvent) deserialized;
    Assertions.assertEquals("GET", event.arguments.getMethod());
    Assertions.assertEquals("http://localhost:8082/v1/sessions", event.arguments.getUrl());
    Assertions.assertEquals(200, event.arguments.getStatus());
    assertNull(event.arguments.getType());
    Assertions.assertEquals("xmlhttprequest", event.arguments.getInitiatorType());
    Assertions.assertEquals("h2", event.arguments.getNextHopProtocol());
    Assertions.assertEquals(BrowserEventType.XHR, event.getEventType());
  }

  @Test
  public void __11__xhr_fetchBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("11__xhr__fetch.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserXhrEvent.class, deserialized.getClass());

    BrowserXhrEvent event = (BrowserXhrEvent) deserialized;
    Assertions.assertEquals("GET", event.arguments.getMethod());
    Assertions.assertEquals("http://localhost:8082/v1/sessions", event.arguments.getUrl());
    Assertions.assertEquals(200, event.arguments.getStatus());
    Assertions.assertEquals("cors", event.arguments.getType());
    Assertions.assertEquals("fetch", event.arguments.getInitiatorType());
    Assertions.assertEquals("http/1.1", event.arguments.getNextHopProtocol());
    Assertions.assertEquals(BrowserEventType.XHR, event.getEventType());
  }

  @Test
  public void __12__resourcePerformanceBeaconEventDeserializationTest()
      throws IOException, URISyntaxException {
    String payload = EventTestData.readIncomingEvent("12__resourceperformance.json");
    AbstractBrowserEvent<?> deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserResourcePerformanceEvent.class, deserialized.getClass());

    BrowserResourcePerformanceEvent event = (BrowserResourcePerformanceEvent) deserialized;
    Assertions.assertEquals("http://localhost:8082/v1/sessions", event.arguments.getName());
    Assertions.assertEquals(20, event.arguments.getStartTime());
    Assertions.assertEquals(40, event.arguments.getDuration());
    Assertions.assertEquals("fetch", event.arguments.getInitiatorType());
    Assertions.assertEquals("http/1.1", event.arguments.getNextHopProtocol());
    Assertions.assertEquals(BrowserEventType.RESOURCE_PERFORMANCE, event.getEventType());
  }
}
