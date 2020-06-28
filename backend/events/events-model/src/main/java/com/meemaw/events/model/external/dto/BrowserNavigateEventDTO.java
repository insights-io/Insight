package com.meemaw.events.model.external.dto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserNavigateEventDTO extends AbstractBrowserEventDTO {

  String location;
  String title;
}
