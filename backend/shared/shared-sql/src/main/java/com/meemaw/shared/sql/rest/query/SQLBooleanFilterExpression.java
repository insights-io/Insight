package com.meemaw.shared.sql.rest.query;

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
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    List<FilterExpression> children = expression.getChildren();
    if (children.isEmpty()) {
      return query;
    }
    SelectConditionStep<?> subQuery = SQLFilterExpression.of(children.get(0)).sql(query, mappings);

    for (int i = 1; i < children.size(); i++) {
      SQLFilterExpression filterExpression = SQLFilterExpression.of(children.get(i));
      if (filterExpression instanceof SQLBooleanFilterExpression) {
        subQuery = filterExpression.sql(query, mappings);
      } else if (filterExpression instanceof SQLTermFilterExpression) {
        SQLTermFilterExpression termFilterExpression = (SQLTermFilterExpression) filterExpression;
        String fieldName = termFilterExpression.getExpression().getField();
        subQuery =
            SQLBooleanOperation.of(expression.getOperation())
                .apply(subQuery, termFilterExpression.condition(mappings.get(fieldName)));
      }
    }

    return subQuery;
  }
}
