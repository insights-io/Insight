package com.rebrowse.events.model.incoming;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class AbstractBrowserPerformanceEventArguments {

  String name;
  double startTime;
  double duration;
}
