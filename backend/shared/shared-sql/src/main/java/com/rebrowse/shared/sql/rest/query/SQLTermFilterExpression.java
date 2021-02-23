package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.shared.rest.query.TermFilterExpression;
import com.rebrowse.api.query.QueryParam;
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
    QueryParam<T> queryParam = expression.getQueryParam();
    return SQLTermCondition.of(queryParam.getCondition()).apply(field, queryParam.getValue());
  }

  @SuppressWarnings("unchecked")
  @Override
  public SelectConditionStep<?> sql(SelectConditionStep<?> query, Map<String, Field<?>> mappings) {
    Field<T> sqlField = (Field<T>) mappings.get(expression.getField());
    return ((SelectJoinStep<?>) query).where(condition(SQLFilterExpression.filterField(sqlField)));
  }
}
