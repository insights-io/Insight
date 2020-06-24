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
  @Type(value = BrowserUnloadEvent.class, name = BrowserEventTypeConstants.UNLOAD),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBrowserEvent extends Recorded {}
