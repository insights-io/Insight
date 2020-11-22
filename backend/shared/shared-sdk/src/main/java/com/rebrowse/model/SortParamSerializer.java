package com.rebrowse.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rebrowse.api.query.SortParam;
import java.io.IOException;

public class SortParamSerializer extends JsonSerializer<SortParam> {

  @Override
  public void serialize(
      SortParam sortParam, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(
        String.format(
            "%c%s", sortParam.getDirection().getSymbol(), String.join(",", sortParam.getFields())));
  }
}
