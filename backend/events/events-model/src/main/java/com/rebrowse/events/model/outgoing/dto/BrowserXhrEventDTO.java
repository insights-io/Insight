package com.rebrowse.events.model.outgoing.dto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserXhrEventDTO extends AbstractBrowserEventDTO {

  String method;
  String url;
  int status;
  String type;
  String initiatorType;
  String nextHopProtocol; // extracted from PerformanceResourceTiming
}
