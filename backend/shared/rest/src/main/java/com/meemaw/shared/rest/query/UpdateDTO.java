package com.meemaw.shared.rest.query;

import com.google.common.base.CaseFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
      if (!updatableFields.contains(entry.getKey())) {
        errors.put(entry.getKey(), "Unexpected field");
      }
    }
    return errors;
  }

  public static Set<String> withCamelCase(Set<String> snakeCaseFields) {
    return Stream.concat(
            snakeCaseFields.stream()
                .map(
                    snakeCaseField ->
                        CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, snakeCaseField)),
            snakeCaseFields.stream())
        .collect(Collectors.toSet());
  }
}
