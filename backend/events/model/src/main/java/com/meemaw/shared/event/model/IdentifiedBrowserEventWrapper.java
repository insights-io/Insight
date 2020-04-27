package com.meemaw.shared.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdentifiedBrowserEventWrapper<T extends AbstractBrowserEvent> {

  @JsonProperty("event")
  T event;

  @JsonProperty("pageId")
  UUID pageId;

}
