package com.rebrowse.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class UserAgent {

  private static final String ROBOT = "Robot";
  private static final String HACKER = "Hacker";

  String deviceClass;
  String operatingSystemName;
  String browserName;

  @JsonIgnore
  public boolean isRobot() {
    return ROBOT.equals(deviceClass);
  }

  @JsonIgnore
  public boolean isHacker() {
    return HACKER.equals(deviceClass);
  }
}
