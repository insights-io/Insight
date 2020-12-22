package com.meemaw.useragent.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserAgentMapper implements HasUserAgent {

  String deviceName;
  String deviceBrand;
  DeviceClass deviceClass;
  String operatingSystemName;
  String operatingSystemVersion;
  String agentName;
  String agentVersion;
}
