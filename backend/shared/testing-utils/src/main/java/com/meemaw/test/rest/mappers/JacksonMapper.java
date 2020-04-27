package com.meemaw.test.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.shared.rest.mappers.JacksonObjectMapperCustomizer;

public class JacksonMapper {

  private static ObjectMapper INSTANCE;

  public static ObjectMapper get() {
    if (INSTANCE == null) {
      INSTANCE = JacksonObjectMapperCustomizer.configure(new ObjectMapper());
    }
    return INSTANCE;
  }
}
