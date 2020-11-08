package com.meemaw.events.model.outgoing.dto;

import static com.meemaw.events.model.incoming.AbstractBrowserEvent.EVENT_TYPE;
import static com.meemaw.events.model.shared.BrowserEventType.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.meemaw.events.model.Recorded;
import com.meemaw.events.model.shared.BrowserEventType;

@JsonTypeInfo(
    use = Id.NAME,
    property = EVENT_TYPE,
    defaultImpl = AbstractBrowserEventDTO.class,
    visible = true)
@JsonSubTypes({
  @Type(value = BrowserNavigateEventDTO.class, name = NAVIGATE_KEY),
  @Type(value = BrowserUnloadEventDTO.class, name = UNLOAD_KEY),
  @Type(value = BrowserResizeEventDTO.class, name = RESIZE_KEY),
  @Type(value = BrowserPerformanceEventDTO.class, name = PERFORMANCE_KEY),
  @Type(value = BrowserClickEventDTO.class, name = CLICK_KEY),
  @Type(value = BrowserMouseMoveEventDTO.class, name = MOUSEMOVE_KEY),
  @Type(value = BrowserMouseDownEventDTO.class, name = MOUSEDOWN_KEY),
  @Type(value = BrowserMouseUpEventDTO.class, name = MOUSEUP_KEY),
  @Type(value = BrowserLoadEventDTO.class, name = LOAD_KEY),
  @Type(value = BrowserLogEventDTO.class, name = LOG_KEY),
  @Type(value = BrowserErrorEventDTO.class, name = ERROR_KEY),
  @Type(value = BrowserXhrEventDTO.class, name = XHR_KEY),
  @Type(value = BrowserResourcePerformanceEventDTO.class, name = RESOURCE_PERFORMANCE_KEY),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBrowserEventDTO extends Recorded {

  @JsonProperty(access = Access.WRITE_ONLY, value = EVENT_TYPE)
  BrowserEventType eventType;

  @JsonProperty(access = Access.READ_ONLY, value = EVENT_TYPE)
  public byte getEventTypeKey() {
    return eventType.getKey();
  }
}
