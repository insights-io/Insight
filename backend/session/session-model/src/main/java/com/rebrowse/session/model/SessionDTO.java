package com.rebrowse.session.model;

import com.rebrowse.location.model.Location;
import com.rebrowse.useragent.model.UserAgent;
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
  Location location;
  UserAgent userAgent;
  OffsetDateTime createdAt;
}
