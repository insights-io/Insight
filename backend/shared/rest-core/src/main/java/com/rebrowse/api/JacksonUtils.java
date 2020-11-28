package com.rebrowse.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonUtils {

  private JacksonUtils() {}

  public static ObjectMapper configure(ObjectMapper mapper) {
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  public static ObjectMapper configureServer(ObjectMapper mapper) {
    return configure(mapper);
  }

  public static ObjectMapper configureClient(ObjectMapper mapper) {
    return configure(mapper);
  }
}
