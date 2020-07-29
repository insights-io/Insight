package com.meemaw.useragent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
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
