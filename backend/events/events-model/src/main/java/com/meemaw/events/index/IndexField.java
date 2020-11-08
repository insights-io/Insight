package com.meemaw.events.index;

import lombok.Value;

import java.util.Map;

@Value
public class IndexField {

  String name;
  Map<String, ?> properties;
}
