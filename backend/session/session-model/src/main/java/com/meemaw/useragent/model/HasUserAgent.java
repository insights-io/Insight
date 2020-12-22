package com.meemaw.useragent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasUserAgent {

  /* "Google Chromecast" | String */
  String getDeviceName();

  /* "Apple" | "Google" | String */
  String getDeviceBrand();

  DeviceClass getDeviceClass();

  /* "Mac OS X" | String */
  String getOperatingSystemName();

  /* "10.14.6" | String */
  String getOperatingSystemVersion();

  /* "Firefox" | "Chrome" | String */
  String getAgentName();

  /* "87.0.664" | String */
  String getAgentVersion();

  @JsonIgnore
  default boolean isRobot() {
    return DeviceClass.ROBOT.equals(getDeviceClass())
        || DeviceClass.ROBOT_IMITATOR.equals(getDeviceClass())
        || DeviceClass.ROBOT_MOBILE.equals(getDeviceClass());
  }

  @JsonIgnore
  default boolean isHacker() {
    return DeviceClass.HACKER.equals(getDeviceClass());
  }

  @JsonIgnore
  default UserAgentMapper mapper() {
    return UserAgentMapper.builder()
        .deviceName(getDeviceName())
        .deviceBrand(getDeviceBrand())
        .deviceClass(getDeviceClass())
        .operatingSystemName(getOperatingSystemName())
        .operatingSystemVersion(getOperatingSystemVersion())
        .agentName(getAgentName())
        .agentVersion(getAgentVersion())
        .build();
  }

  @JsonIgnore
  default UserAgent userAgent() {
    return UserAgent.builder()
        .deviceName(getDeviceName())
        .deviceBrand(getDeviceBrand())
        .deviceClass(getDeviceClass())
        .operatingSystemName(getOperatingSystemName())
        .operatingSystemVersion(getOperatingSystemVersion())
        .agentName(getAgentName())
        .agentVersion(getAgentVersion())
        .build();
  }
}
