package com.rebrowse.shared.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.api.JacksonUtils;
import io.quarkus.jackson.ObjectMapperCustomizer;
import javax.inject.Singleton;

@Singleton
public class JacksonObjectMapperCustomizer implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper mapper) {
    JacksonUtils.configureServer(mapper);
  }
}
