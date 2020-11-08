package com.meemaw.session.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.useragent.model.UserAgentDTO;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SessionDTO {

  UUID id;
  UUID deviceId;
  String organizationId;
  LocationDTO location;
  UserAgentDTO userAgent;
  OffsetDateTime createdAt;
}
