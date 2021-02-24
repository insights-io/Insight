package com.rebrowse.shared.sql.rest.query;

import com.google.common.base.CaseFormat;
import com.rebrowse.shared.rest.query.UpdateDTO;
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
      String sqlField = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
      Field field = mappings.get(sqlField);
      updateStep.set(field, entry.getValue());
    }
    return (UpdateFromStep<?>) updateStep;
  }

  public static SQLUpdateDTO of(UpdateDTO update) {
    return new SQLUpdateDTO(update);
  }
}
