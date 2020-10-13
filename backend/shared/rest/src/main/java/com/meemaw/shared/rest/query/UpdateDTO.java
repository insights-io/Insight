package com.meemaw.shared.rest.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Value;

@Value
public class UpdateDTO {

  Map<String, Object> params;
  Set<String> updatableFields;

  public static UpdateDTO from(Map<String, Object> params, Set<String> updatableFields) {
    return new UpdateDTO(params, updatableFields);
  }

  public Map<String, String> validate() {
    Map<String, String> errors = new HashMap<>();
    for (Entry<String, ?> entry : params.entrySet()) {
      if (!updatableFields.contains(entry.getKey())) {
        errors.put(entry.getKey(), "Unexpected field");
      }
    }
    return errors;
  }
}
