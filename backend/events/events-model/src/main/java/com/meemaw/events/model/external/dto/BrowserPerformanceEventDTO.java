package com.meemaw.events.model.external.dto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserPerformanceEventDTO extends AbstractBrowserEventDTO {

  String name;
  String entryType;
  double startTime;
  double duration;
}
