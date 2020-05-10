package com.meemaw.shared.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.model.internal.BrowserClickEvent;
import com.meemaw.events.model.internal.BrowserLoadEvent;
import com.meemaw.events.model.internal.BrowserMouseMoveEvent;
import com.meemaw.events.model.internal.BrowserNavigateEvent;
import com.meemaw.events.model.internal.BrowserPerformanceEvent;
import com.meemaw.events.model.internal.BrowserResizeEvent;
import com.meemaw.events.model.internal.BrowserUnloadEvent;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class AbstractBrowserEventSerializationTest {

  @Test
  public void loadBeaconEventDeserialization() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 8, \"a\": [\"http://localhost:8080\"]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserLoadEvent.class, deserialized.getClass());

    BrowserLoadEvent browserUnloadEvent = (BrowserLoadEvent) deserialized;
    assertEquals("http://localhost:8080", browserUnloadEvent.getLocation());
  }

  @Test
  public void unloadBeaconEventDeserialization() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 1, \"a\": [\"http://localhost:8080\"]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserUnloadEvent.class, deserialized.getClass());

    BrowserUnloadEvent browserUnloadEvent = (BrowserUnloadEvent) deserialized;
    assertEquals("http://localhost:8080", browserUnloadEvent.getLocation());
  }

  @Test
  public void resizeBeaconEventDeserialization() throws JsonProcessingException {
    String payload = "{\"t\": 1234, \"e\": 2, \"a\": [100, 200]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserResizeEvent.class, deserialized.getClass());

    BrowserResizeEvent browserResizeEvent = (BrowserResizeEvent) deserialized;
    assertEquals(100, browserResizeEvent.getInnerWidth());
    assertEquals(200, browserResizeEvent.getInnerHeight());
  }

  @Test
  public void performanceBeaconEventDeserialization() throws JsonProcessingException {
    String payload =
        "{\"t\": 13097,\"e\": 3,\"a\": [\"⚛ FormControl [update]\", \"measure\", 18549.754999927245, 1.9050000701099634]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserPerformanceEvent.class, deserialized.getClass());

    BrowserPerformanceEvent browserPerformanceEvent = (BrowserPerformanceEvent) deserialized;
    assertEquals("⚛ FormControl [update]", browserPerformanceEvent.getName());
    assertEquals("measure", browserPerformanceEvent.getEntryType());
    assertEquals(18549.754999927245, browserPerformanceEvent.getStartTime());
    assertEquals(1.9050000701099634, browserPerformanceEvent.getDuration());
  }

  @Test
  public void performanceBeaconEventDeserialization2() throws JsonProcessingException {
    String payload =
        "{\"t\": 17,\"e\": 3,\"a\": [\"http://localhost:3002/\", \"navigation\", 0, 5478.304999996908]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);

    assertEquals(BrowserPerformanceEvent.class, deserialized.getClass());

    BrowserPerformanceEvent browserPerformanceEvent = (BrowserPerformanceEvent) deserialized;
    assertEquals("http://localhost:3002/", browserPerformanceEvent.getName());
    assertEquals("navigation", browserPerformanceEvent.getEntryType());
    assertEquals(0, browserPerformanceEvent.getStartTime());
    assertEquals(5478.304999996908, browserPerformanceEvent.getDuration());
  }

  @Test
  public void navigateBeaconEventDeserialization() throws JsonProcessingException {
    String payload =
        "{\"t\": 1234, \"e\": 0, \"a\": [\"http://localhost:8080/test\", \"Test title\"]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserNavigateEvent.class, deserialized.getClass());

    BrowserNavigateEvent browserNavigateEvent = (BrowserNavigateEvent) deserialized;
    assertEquals("http://localhost:8080/test", browserNavigateEvent.getLocation());
    assertEquals("Test title", browserNavigateEvent.getTitle());
  }

  @Test
  public void clickBeaconEventDeserialization() throws JsonProcessingException {
    String payload =
        "{\"t\": 1306,\"e\": 4,\"a\": [1167, 732, \"<BUTTON\", \":data-baseweb\", \"button\", \":type\", \"submit\", \":class\", \"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\"]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserClickEvent.class, deserialized.getClass());

    BrowserClickEvent browserClickEvent = (BrowserClickEvent) deserialized;
    assertEquals(1167, browserClickEvent.getClientX());
    assertEquals(732, browserClickEvent.getClientY());
    assertEquals("BUTTON", browserClickEvent.getNode().get());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        browserClickEvent.getNodeWithAttributes());
  }

  @Test
  public void clickBeaconEventDeserialization2() throws JsonProcessingException {
    String payload = "{\"t\": 1306,\"e\": 4,\"a\": [1167, 732]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserClickEvent.class, deserialized.getClass());

    BrowserClickEvent browserClickEvent = (BrowserClickEvent) deserialized;
    assertEquals(1167, browserClickEvent.getClientX());
    assertEquals(732, browserClickEvent.getClientY());
    assertEquals(Optional.empty(), browserClickEvent.getNode());
    assertEquals(Collections.emptyList(), browserClickEvent.getNodeWithAttributes());
  }

  @Test
  public void mouseMoveBeaconEventDeserialization() throws JsonProcessingException {
    String payload =
        "{\"t\": 1306,\"e\": 5,\"a\": [1167, 732, \"<BUTTON\", \":data-baseweb\", \"button\", \":type\", \"submit\", \":class\", \"__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw\"]}";
    AbstractBrowserEvent deserialized =
        JacksonMapper.get().readValue(payload, AbstractBrowserEvent.class);
    assertEquals(BrowserMouseMoveEvent.class, deserialized.getClass());

    BrowserMouseMoveEvent browserMouseMoveEvent = (BrowserMouseMoveEvent) deserialized;
    assertEquals(1167, browserMouseMoveEvent.getClientX());
    assertEquals(732, browserMouseMoveEvent.getClientY());
    assertEquals("BUTTON", browserMouseMoveEvent.getNode().get());
    assertEquals(
        List.of(
            "<BUTTON",
            ":data-baseweb",
            "button",
            ":type",
            "submit",
            ":class",
            "__debug-3 as at au av aw ax ay az b0 b1 b2 b3 b4 b5 b6 ak b7 b8 b9 ba bb bc bd be bf bg bh bi an ci ao c8 d8 d9 d7 da ek el em df en eo ep eq bw"),
        browserMouseMoveEvent.getNodeWithAttributes());
  }
}
