package com.meemaw.rest.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;

@Singleton
public class JacksonObjectMapperCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        configure(mapper);
    }

    public static ObjectMapper configure(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
