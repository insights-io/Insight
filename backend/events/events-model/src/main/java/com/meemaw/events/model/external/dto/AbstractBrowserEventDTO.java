package com.meemaw.events.model.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.meemaw.events.model.Recorded;
import com.meemaw.events.model.internal.BrowserEventTypeConstants;
import lombok.ToString;

@ToString(callSuper = true)
@JsonTypeInfo(
    use = Id.NAME,
    property = com.meemaw.events.model.internal.AbstractBrowserEvent.EVENT_TYPE,
    defaultImpl = com.meemaw.events.model.internal.AbstractBrowserEvent.class)
@JsonSubTypes({
  @Type(value = BrowserNavigateEventDTO.class, name = BrowserEventTypeConstants.NAVIGATE),
  @Type(value = BrowserUnloadEventDTO.class, name = BrowserEventTypeConstants.UNLOAD),
  @Type(value = BrowserResizeEventDTO.class, name = BrowserEventTypeConstants.RESIZE),
  @Type(value = BrowserPerformanceEventDTO.class, name = BrowserEventTypeConstants.PERFORMANCE),
  @Type(value = BrowserClickEventDTO.class, name = BrowserEventTypeConstants.CLICK),
  @Type(value = BrowserMouseMoveEventDTO.class, name = BrowserEventTypeConstants.MOUSEMOVE),
  @Type(value = BrowserMouseDownEventDTO.class, name = BrowserEventTypeConstants.MOUSEDOWN),
  @Type(value = BrowserMouseUpEventDTO.class, name = BrowserEventTypeConstants.MOUSEUP),
  @Type(value = BrowserLoadEventDTO.class, name = BrowserEventTypeConstants.LOAD),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBrowserEventDTO extends Recorded {}
