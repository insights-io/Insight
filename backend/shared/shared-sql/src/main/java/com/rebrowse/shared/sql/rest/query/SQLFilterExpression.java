package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.shared.rest.query.BooleanFilterExpression;
import com.rebrowse.shared.rest.query.FilterExpression;
import com.rebrowse.shared.rest.query.TermFilterExpression;
import java.util.Map;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

public interface SQLFilterExpression extends FilterExpression {

  @SuppressWarnings({"unchecked", "rawtypes"})
  static SQLFilterExpression of(FilterExpression filterExpression) {
    if (filterExpression instanceof BooleanFilterExpression) {
      return new SQLBooleanFilterExpression((BooleanFilterExpression) filterExpression);
    } else {
      return new SQLTermFilterExpression((TermFilterExpression) filterExpression);
    }
  }

  static <T> Field<T> selectField(String field, Class<T> dataType, String separator) {
    String[] split = field.split("\\.");
    String result = split[0];

    for (int i = 1; i < split.length; i++) {
      result = String.format("%s %s '%s'", result, separator, split[i]);
    }
    return DSL.field(result, dataType);
  }

  static <T> Field<T> filterField(Field<T> field) {
    return jsonText(field.getName(), field.getType());
  }

  static <T> Field<T> jsonb(String field, Class<T> dataType) {
    return selectField(field, dataType, "->");
  }

  static <T> Field<T> jsonText(String field, Class<T> dataType) {
    return selectField(field, dataType, "->>");
  }

  /**
   * Update query with filter expression.
   *
   * @param query existing select condition query
   * @param mappings field mappings
   * @return query with applied filter conditions
   */
  SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> mappings);

  /**
   * Update query with filter expression.
   *
   * @param query existing select join query
   * @param mappings fields mappings
   * @return query with applied filter conditions
   */
  @SuppressWarnings({"rawtypes"})
  default SelectConditionStep<?> sql(SelectJoinStep<?> query, Map<String, Field<?>> mappings) {
    return sql((SelectConditionStep) query, mappings);
  }
}
