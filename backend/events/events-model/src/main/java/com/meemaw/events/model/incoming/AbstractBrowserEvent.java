package com.meemaw.events.model.incoming;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meemaw.events.model.Recorded;
import com.meemaw.events.model.shared.BrowserEventType;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
    use = Id.NAME,
    property = AbstractBrowserEvent.EVENT_TYPE,
    defaultImpl = AbstractBrowserEvent.class,
    visible = true)
@JsonSubTypes({
  @Type(value = BrowserNavigateEvent.class, name = BrowserEventType.NAVIGATE_KEY),
  @Type(value = BrowserUnloadEvent.class, name = BrowserEventType.UNLOAD_KEY),
  @Type(value = BrowserResizeEvent.class, name = BrowserEventType.RESIZE_KEY),
  @Type(value = BrowserPerformanceEvent.class, name = BrowserEventType.PERFORMANCE_KEY),
  @Type(value = BrowserClickEvent.class, name = BrowserEventType.CLICK_KEY),
  @Type(value = BrowserMouseMoveEvent.class, name = BrowserEventType.MOUSEMOVE_KEY),
  @Type(value = BrowserMouseDownEvent.class, name = BrowserEventType.MOUSEDOWN_KEY),
  @Type(value = BrowserMouseUpEvent.class, name = BrowserEventType.MOUSEUP_KEY),
  @Type(value = BrowserLoadEvent.class, name = BrowserEventType.LOAD_KEY),
  @Type(value = BrowserLogEvent.class, name = BrowserEventType.LOG_KEY),
  @Type(value = BrowserErrorEvent.class, name = BrowserEventType.ERROR_KEY),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBrowserEvent<T> extends Recorded {

  public static final String EVENT_TYPE = "e";
  public static final String ARGS = "a";

  @JsonProperty(access = Access.WRITE_ONLY, value = EVENT_TYPE)
  BrowserEventType eventType;

  @JsonProperty(access = Access.READ_ONLY, value = EVENT_TYPE)
  public byte getEventTypeKey() {
    return eventType.getKey();
  }

  @JsonProperty(ARGS)
  @JsonFormat(shape = JsonFormat.Shape.ARRAY)
  T arguments;

  @JsonIgnore
  public abstract Map<String, Object> index();
}
