package com.rebrowse.events.model.incoming;

import static com.rebrowse.events.model.incoming.AbstractBrowserEvent.EVENT_TYPE;
import static com.rebrowse.events.model.shared.BrowserEventType.CLICK_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.ERROR_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.LOAD_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.LOG_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.MOUSEDOWN_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.MOUSEMOVE_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.MOUSEUP_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.NAVIGATE_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.PERFORMANCE_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.RESIZE_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.RESOURCE_PERFORMANCE_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.UNLOAD_KEY;
import static com.rebrowse.events.model.shared.BrowserEventType.XHR_KEY;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.rebrowse.events.model.Recorded;
import com.rebrowse.events.model.shared.BrowserEventType;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
    use = Id.NAME,
    property = EVENT_TYPE,
    defaultImpl = AbstractBrowserEvent.class,
    visible = true)
@JsonSubTypes({
  @Type(value = BrowserNavigateEvent.class, name = NAVIGATE_KEY),
  @Type(value = BrowserUnloadEvent.class, name = UNLOAD_KEY),
  @Type(value = BrowserResizeEvent.class, name = RESIZE_KEY),
  @Type(value = BrowserPerformanceEvent.class, name = PERFORMANCE_KEY),
  @Type(value = BrowserClickEvent.class, name = CLICK_KEY),
  @Type(value = BrowserMouseMoveEvent.class, name = MOUSEMOVE_KEY),
  @Type(value = BrowserMouseDownEvent.class, name = MOUSEDOWN_KEY),
  @Type(value = BrowserMouseUpEvent.class, name = MOUSEUP_KEY),
  @Type(value = BrowserLoadEvent.class, name = LOAD_KEY),
  @Type(value = BrowserLogEvent.class, name = LOG_KEY),
  @Type(value = BrowserErrorEvent.class, name = ERROR_KEY),
  @Type(value = BrowserXhrEvent.class, name = XHR_KEY),
  @Type(value = BrowserResourcePerformanceEvent.class, name = RESOURCE_PERFORMANCE_KEY),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBrowserEvent<T> extends Recorded {

  public static final String EVENT_TYPE = "e";
  public static final String ARGS = "a";

  @JsonProperty(access = Access.WRITE_ONLY, value = EVENT_TYPE)
  BrowserEventType eventType;

  @JsonProperty(ARGS)
  @JsonFormat(shape = JsonFormat.Shape.ARRAY)
  T arguments;

  @JsonProperty(access = Access.READ_ONLY, value = EVENT_TYPE)
  public byte getEventTypeKey() {
    return eventType.getKey();
  }

  @JsonIgnore
  public abstract Map<String, Object> index();
}
