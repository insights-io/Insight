package com.meemaw.shared.rest.query.sql;

import com.meemaw.shared.rest.query.TermFilterExpression;
import java.util.Map;
import lombok.Value;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

@Value
public class SQLTermFilterExpression<T> implements SQLFilterExpression {

  TermFilterExpression<T> expression;

  public Condition condition(Field<T> field) {
    return SQLTermOperation.of(expression.getOperation()).apply(field, expression.getTarget());
  }

  @SuppressWarnings("unchecked")
  @Override
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> fields) {
    Field<T> sqlField = (Field<T>) fields.get(expression.getField());
    return ((SelectJoinStep<?>) query).where(condition(sqlField));
  }
}
