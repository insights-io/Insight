package com.meemaw.session.model;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PageVisitDTO {

  UUID id;
  UUID sessionId;
  String organizationId;
  String doctype;
  URL origin;
  String path;
  String referrer;
  int height;
  int width;
  int screenHeight;
  int screenWidth;
  long compiledTimestamp;
  OffsetDateTime pageStart;
}
