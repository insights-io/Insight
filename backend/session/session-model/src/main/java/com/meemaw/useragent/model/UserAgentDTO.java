package com.meemaw.useragent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UserAgentDTO {

  String deviceClass;
  String operatingSystemName;
  String browserName;

  @JsonIgnore
  public boolean isRobot() {
    return "Robot".equals(deviceClass);
  }

  @JsonIgnore
  public boolean isHacker() {
    return "Hacker".equals(deviceClass);
  }
}
