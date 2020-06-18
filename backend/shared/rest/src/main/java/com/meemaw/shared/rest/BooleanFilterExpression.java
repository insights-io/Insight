package com.meemaw.shared.rest;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

@Value
@EqualsAndHashCode(callSuper = true)
public class BooleanFilterExpression extends FilterExpression {

  BooleanOperation operator;
  List<FilterExpression> children;

  @Override
  public SelectConditionStep<?> sql(SelectJoinStep<?> query) {
    if (children.size() == 0) {
      return (SelectConditionStep<?>) query;
    }
    SelectConditionStep<?> subQuery = children.get(0).sql(query);

    for (int i = 1; i < children.size(); i++) {
      FilterExpression filterExpression = children.get(i);
      if (filterExpression instanceof BooleanFilterExpression) {
        subQuery = ((BooleanFilterExpression) filterExpression).sql(query);
      } else if (filterExpression instanceof TermFilterExpression) {
        subQuery =
            operator.applyCondition(
                subQuery, ((TermFilterExpression<?>) filterExpression).condition());
      } else {
        throw new IllegalStateException("Unsupported filter expression");
      }
    }

    return subQuery;
  }
}
