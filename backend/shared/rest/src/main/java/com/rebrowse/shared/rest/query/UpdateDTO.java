package com.rebrowse.shared.rest.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Value;

@Value
public class UpdateDTO {

  Map<String, Object> params;

  public static UpdateDTO from(Map<String, Object> params) {
    return new UpdateDTO(params);
  }

  public Map<String, String> validate(Set<String> updatableFields) {
    Map<String, String> errors = new HashMap<>();
    for (Entry<String, ?> entry : params.entrySet()) {
      String field = AbstractQueryParser.snakeCase(entry.getKey());
      if (!updatableFields.contains(field)) {
        errors.put(field, "Unexpected field");
      }
    }
    return errors;
  }
}
