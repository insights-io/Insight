package com.meemaw.test.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.api.JacksonUtils;

public final class JacksonMapper {

  private static ObjectMapper INSTANCE;

  private JacksonMapper() {}

  public static ObjectMapper get() {
    if (INSTANCE == null) {
      INSTANCE = JacksonUtils.configure(new ObjectMapper());
    }
    return INSTANCE;
  }
}
