package com.meemaw.events.model.outgoing.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.meemaw.events.model.shared.LogLevel;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class BrowserLogEventDTO extends AbstractBrowserEventDTO {

  LogLevel level;
  List<String> arguments;
}
