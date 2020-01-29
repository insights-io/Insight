package com.meemaw.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.rest.mapper.JacksonObjectMapperCustomizer;

public class JacksonMapper {

    private static ObjectMapper INSTANCE;

    public static ObjectMapper get() {
        if (INSTANCE == null) {
            INSTANCE = JacksonObjectMapperCustomizer.configure(new com.fasterxml.jackson.databind.ObjectMapper());
        }
        return INSTANCE;
    }
}
