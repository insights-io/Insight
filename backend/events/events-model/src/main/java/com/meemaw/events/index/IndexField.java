package com.meemaw.events.index;

import java.util.Map;
import lombok.Value;

@Value
public class IndexField {

  String name;
  Map<String, ?> properties;
}
