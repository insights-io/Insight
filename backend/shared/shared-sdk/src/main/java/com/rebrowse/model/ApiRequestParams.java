package com.rebrowse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.exception.JsonException;
import com.rebrowse.net.ApiResource;
import java.util.Map;

public interface ApiRequestParams {

  default String writeValueAsString() {
    try {
      return ApiResource.OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException exception) {
      throw new JsonException(exception);
    }
  }

  default Map<String, Object> toMap() {
    return ApiResource.OBJECT_MAPPER.convertValue(this, new TypeReference<>() {});
  }
}
