package com.meemaw.shared.sql.rest.query;

import com.meemaw.shared.rest.query.UpdateDTO;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Value;
import org.jooq.Field;
import org.jooq.UpdateFromStep;
import org.jooq.UpdateSetFirstStep;

@Value
public class SQLUpdateDTO {

  UpdateDTO update;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public UpdateFromStep<?> apply(UpdateSetFirstStep<?> updateStep, Map<String, Field<?>> mappings) {
    for (Entry<String, ?> entry : update.getParams().entrySet()) {
      Field field = mappings.get(entry.getKey());
      updateStep.set(field, entry.getValue());
    }
    return (UpdateFromStep<?>) updateStep;
  }

  public static SQLUpdateDTO of(UpdateDTO update) {
    return new SQLUpdateDTO(update);
  }
}
