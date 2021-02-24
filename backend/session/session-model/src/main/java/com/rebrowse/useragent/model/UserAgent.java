package com.rebrowse.useragent.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@Builder
public class UserAgent implements HasUserAgent {

  String deviceName;
  String deviceBrand;
  DeviceClass deviceClass;
  String operatingSystemName;
  String operatingSystemVersion;
  String agentName;
  String agentVersion;
}
