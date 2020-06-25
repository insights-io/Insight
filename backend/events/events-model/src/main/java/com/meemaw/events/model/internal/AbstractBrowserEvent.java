package com.meemaw.events.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meemaw.events.model.Recorded;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonTypeInfo(
    use = Id.NAME,
    property = AbstractBrowserEvent.EVENT_TYPE,
    defaultImpl = AbstractBrowserEvent.class)
@JsonSubTypes({
  @Type(value = BrowserNavigateEvent.class, name = BrowserEventTypeConstants.NAVIGATE),
  @Type(value = BrowserUnloadEvent.class, name = BrowserEventTypeConstants.UNLOAD),
  @Type(value = BrowserResizeEvent.class, name = BrowserEventTypeConstants.RESIZE),
  @Type(value = BrowserPerformanceEvent.class, name = BrowserEventTypeConstants.PERFORMANCE),
  @Type(value = BrowserClickEvent.class, name = BrowserEventTypeConstants.CLICK),
  @Type(value = BrowserMouseMoveEvent.class, name = BrowserEventTypeConstants.MOUSEMOVE),
  @Type(value = BrowserMouseDownEvent.class, name = BrowserEventTypeConstants.MOUSEDOWN),
  @Type(value = BrowserMouseUpEvent.class, name = BrowserEventTypeConstants.MOUSEUP),
  @Type(value = BrowserLoadEvent.class, name = BrowserEventTypeConstants.LOAD),
  @Type(value = BrowserLogEvent.class, name = BrowserEventTypeConstants.LOG),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBrowserEvent extends Recorded {

  public static final String EVENT_TYPE = "e";

  @Getter
  @JsonProperty("a")
  List<Object> args;

  @JsonIgnore
  public abstract Map<String, Object> index();

  public abstract String getEventType();
}
