package com.meemaw.auth.organization.model;

import java.time.OffsetDateTime;
import org.apache.commons.lang3.RandomStringUtils;

public interface Organization {

  int ID_LENGTH = 6;

  String getId();

  String getName();

  OffsetDateTime getCreatedAt();

  static String identifier() {
    return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
  }
}
