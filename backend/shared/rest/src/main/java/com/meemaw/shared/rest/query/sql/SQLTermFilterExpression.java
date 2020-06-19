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

  TermFilterExpression<T> termFilterExpression;

  public Condition condition(Field<T> field) {
    return termFilterExpression.getOperation().sql(field, termFilterExpression.getTarget());
  }

  @SuppressWarnings("unchecked")
  @Override
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> fields) {
    Field<T> sqlField = (Field<T>) fields.get(termFilterExpression.getField());
    return ((SelectJoinStep<?>) query).where(condition(sqlField));
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  public SelectConditionStep<?> sql(SelectJoinStep<?> query, Map<String, Field<?>> fields) {
    return sql((SelectConditionStep) query, fields);
  }
}
