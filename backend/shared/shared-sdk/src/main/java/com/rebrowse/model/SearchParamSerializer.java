package com.rebrowse.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class SearchParamSerializer extends JsonSerializer<SearchParam<?>> {

  @Override
  public void serialize(
      SearchParam searchParam, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(
        String.format(
            "%s:%s", searchParam.getOperation().name().toLowerCase(), searchParam.getValue()));
  }
}
