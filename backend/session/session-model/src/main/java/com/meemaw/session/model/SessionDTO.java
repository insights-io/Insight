package com.meemaw.session.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SessionDTO {

  UUID id;
  UUID deviceId;
  String organizationId;
  String ipAddress;
  String userAgent;
  OffsetDateTime createdAt;
}
