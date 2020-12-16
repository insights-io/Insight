package com.meemaw.useragent.model;

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

  String deviceClass;
  String operatingSystemName;
  String browserName;
}
