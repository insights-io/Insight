package com.meemaw.shared.sql.rest.query;

import lombok.Value;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

import com.meemaw.shared.rest.query.TermFilterExpression;

import java.util.Map;

@Value
public class SQLTermFilterExpression<T> implements SQLFilterExpression {

  TermFilterExpression<T> expression;

  public Condition condition(Field<T> field) {
    return SQLTermOperation.of(expression.getOperation()).apply(field, expression.getTarget());
  }

  @SuppressWarnings("unchecked")
  @Override
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    Field<T> sqlField = (Field<T>) mappings.get(expression.getField());
    return ((SelectJoinStep<?>) query)
        .where(condition(SQLFilterExpression.sqlFilterField(sqlField)));
  }
}
