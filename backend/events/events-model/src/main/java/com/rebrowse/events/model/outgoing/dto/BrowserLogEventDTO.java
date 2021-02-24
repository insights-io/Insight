package com.rebrowse.events.model.outgoing.dto;

import com.rebrowse.events.model.shared.LogLevel;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserLogEventDTO extends AbstractBrowserEventDTO {

  LogLevel level;
  List<String> arguments;
}
