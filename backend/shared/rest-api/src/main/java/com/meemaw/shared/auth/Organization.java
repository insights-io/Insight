package com.meemaw.shared.auth;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;

@UtilityClass
public class Organization {

  public final int ORG_ID_LENGTH = 6;

  public String identifier() {
    return RandomStringUtils.randomAlphanumeric(ORG_ID_LENGTH);
  }
}
