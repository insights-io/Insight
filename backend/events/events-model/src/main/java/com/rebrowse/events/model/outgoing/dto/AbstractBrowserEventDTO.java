package com.rebrowse.events.model.outgoing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.rebrowse.events.model.Recorded;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.model.shared.BrowserEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(
    use = Id.NAME,
    property = AbstractBrowserEvent.EVENT_TYPE,
    defaultImpl = AbstractBrowserEventDTO.class,
    visible = true)
@JsonSubTypes({
  @Type(value = BrowserNavigateEventDTO.class, name = BrowserEventType.NAVIGATE_KEY),
  @Type(value = BrowserUnloadEventDTO.class, name = BrowserEventType.UNLOAD_KEY),
  @Type(value = BrowserResizeEventDTO.class, name = BrowserEventType.RESIZE_KEY),
  @Type(value = BrowserPerformanceEventDTO.class, name = BrowserEventType.PERFORMANCE_KEY),
  @Type(value = BrowserClickEventDTO.class, name = BrowserEventType.CLICK_KEY),
  @Type(value = BrowserMouseMoveEventDTO.class, name = BrowserEventType.MOUSEMOVE_KEY),
  @Type(value = BrowserMouseDownEventDTO.class, name = BrowserEventType.MOUSEDOWN_KEY),
  @Type(value = BrowserMouseUpEventDTO.class, name = BrowserEventType.MOUSEUP_KEY),
  @Type(value = BrowserLoadEventDTO.class, name = BrowserEventType.LOAD_KEY),
  @Type(value = BrowserLogEventDTO.class, name = BrowserEventType.LOG_KEY),
  @Type(value = BrowserErrorEventDTO.class, name = BrowserEventType.ERROR_KEY),
  @Type(value = BrowserXhrEventDTO.class, name = BrowserEventType.XHR_KEY),
  @Type(
      value = BrowserResourcePerformanceEventDTO.class,
      name = BrowserEventType.RESOURCE_PERFORMANCE_KEY),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBrowserEventDTO extends Recorded {

  @JsonProperty(access = Access.WRITE_ONLY, value = AbstractBrowserEvent.EVENT_TYPE)
  BrowserEventType eventType;

  @JsonProperty(access = Access.READ_ONLY, value = AbstractBrowserEvent.EVENT_TYPE)
  public byte getEventTypeKey() {
    return eventType.getKey();
  }
}
