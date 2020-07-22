package com.meemaw.useragent.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UserAgentDTO {

  String deviceClass;
  String operatingSystemName;
  String browserName;
}
