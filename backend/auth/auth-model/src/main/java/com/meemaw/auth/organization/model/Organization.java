package com.meemaw.auth.organization.model;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.RandomStringUtils;

public interface Organization {

  int ORG_ID_LENGTH = 6;

  String getId();

  String getName();

  OffsetDateTime getCreatedAt();

  static String identifier() {
    return RandomStringUtils.randomAlphanumeric(ORG_ID_LENGTH);
  }
}
