package com.rebrowse.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class UserAgent {

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
