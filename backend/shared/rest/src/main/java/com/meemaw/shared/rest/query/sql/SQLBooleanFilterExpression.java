package com.meemaw.shared.rest.query.sql;

import com.meemaw.shared.rest.query.BooleanFilterExpression;
import com.meemaw.shared.rest.query.FilterExpression;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.jooq.Field;
import org.jooq.SelectConditionStep;

@Value
public class SQLBooleanFilterExpression implements SQLFilterExpression {

  BooleanFilterExpression<FilterExpression> expression;

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> fields) {
    List<FilterExpression> children = expression.getChildren();
    if (children.isEmpty()) {
      return query;
    }
    SelectConditionStep<?> subQuery = SQLFilterExpression.of(children.get(0)).sql(query, fields);

    for (int i = 1; i < children.size(); i++) {
      SQLFilterExpression filterExpression = SQLFilterExpression.of(children.get(i));
      if (filterExpression instanceof SQLBooleanFilterExpression) {
        subQuery = ((SQLBooleanFilterExpression) filterExpression).sql(query, fields);
      } else if (filterExpression instanceof SQLTermFilterExpression) {
        SQLTermFilterExpression termFilterExpression = (SQLTermFilterExpression) filterExpression;
        String fieldName = termFilterExpression.getExpression().getField();

        if (fields.containsKey(fieldName)) {
          subQuery =
              SQLBooleanOperation.of(expression.getOperation())
                  .apply(subQuery, termFilterExpression.condition(fields.get(fieldName)));
        }
      } else {
        throw new IllegalStateException("Unsupported filter expression");
      }
    }

    return subQuery;
  }
}
