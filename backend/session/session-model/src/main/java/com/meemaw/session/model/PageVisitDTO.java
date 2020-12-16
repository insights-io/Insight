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
public class PageVisitDTO {

  UUID id;
  UUID sessionId;
  String organizationId;
  String doctype;
  String url;
  String referrer;
  int height;
  int width;
  int screenHeight;
  int screenWidth;
  long compiledTimestamp;
  OffsetDateTime pageStart;
}
