package com.meemaw.useragent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasUserAgent {

  String getDeviceClass();

  String getOperatingSystemName();

  String getBrowserName();

  @JsonIgnore
  default boolean isRobot() {
    return "Robot".equals(getDeviceClass());
  }

  @JsonIgnore
  default boolean isHacker() {
    return "Hacker".equals(getDeviceClass());
  }

  @JsonIgnore
  default UserAgentMapper mapper() {
    return UserAgentMapper.builder()
        .deviceClass(getDeviceClass())
        .operatingSystemName(getOperatingSystemName())
        .browserName(getBrowserName())
        .build();
  }

  @JsonIgnore
  default UserAgent userAgent() {
    return UserAgent.builder()
        .deviceClass(getDeviceClass())
        .operatingSystemName(getOperatingSystemName())
        .browserName(getBrowserName())
        .build();
  }
}
