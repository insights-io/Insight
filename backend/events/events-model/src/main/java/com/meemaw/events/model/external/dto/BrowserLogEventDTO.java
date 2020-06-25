package com.meemaw.events.model.external.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserLogEventDTO extends AbstractBrowserEventDTO {

  String level;
  List<String> arguments;
}
