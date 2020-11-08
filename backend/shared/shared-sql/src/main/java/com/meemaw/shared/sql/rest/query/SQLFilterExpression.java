package com.meemaw.shared.sql.rest.query;

import com.meemaw.shared.rest.query.BooleanFilterExpression;
import com.meemaw.shared.rest.query.FilterExpression;
import com.meemaw.shared.rest.query.TermFilterExpression;
import java.util.Map;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

public interface SQLFilterExpression extends FilterExpression {

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

  @SuppressWarnings({"unchecked", "rawtypes"})
  static SQLFilterExpression of(FilterExpression filterExpression) {
    if (filterExpression instanceof BooleanFilterExpression) {
      return new SQLBooleanFilterExpression((BooleanFilterExpression) filterExpression);
    } else {
      return new SQLTermFilterExpression((TermFilterExpression) filterExpression);
    }
  }

  static <T> Field<T> sqlField(String field, Class<T> dataType, String separator) {
    String[] split = field.split("\\.");
    String result = split[0];

    for (int i = 1; i < split.length; i++) {
      result = String.format("%s %s '%s'", result, separator, split[i]);
    }
    return DSL.field(result, dataType);
  }

  static <T> Field<T> sqlSelectField(String field, Class<T> dataType) {
    return sqlField(field, dataType, "->");
  }

  static <T> Field<T> sqlFilterField(String field, Class<T> dataType) {
    return sqlField(field, dataType, "->>");
  }

  static <T> Field<T> sqlFilterField(Field<T> field) {
    return sqlFilterField(field.getName(), field.getType());
  }
}
