package com.meemaw.events.model.outgoing.dto;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserClickEventDTO extends AbstractBrowserEventDTO {

  int clientX;
  int clientY;
  Map<String, ?> node;
}
