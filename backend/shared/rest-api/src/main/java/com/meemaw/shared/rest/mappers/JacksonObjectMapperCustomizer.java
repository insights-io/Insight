package com.meemaw.shared.rest.mappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import javax.inject.Singleton;

@Singleton
public class JacksonObjectMapperCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper mapper) {
    configure(mapper);
  }

  /**
   * @param mapper
   * @return
   */
  public static ObjectMapper configure(ObjectMapper mapper) {
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
