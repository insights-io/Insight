package com.meemaw.shared.rest;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jooq.SelectConditionStep;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectJoinStep;

@Value
@EqualsAndHashCode(callSuper = true)
public class BooleanFilterExpression extends FilterExpression<SelectJoinStep<?>> {

  BooleanOperation operator;
  List<FilterExpression<?>> children;

  public SelectConnectByStep<?> sql(SelectJoinStep<?> query) {
    if (children.size() == 0) {
      return query;
    }
    SelectConditionStep<?> subQuery =
        query.where(termFilterExpression(children.get(0)).condition());

    for (int i = 1; i < children.size(); i++) {
      subQuery =
          operator.applyCondition(subQuery, termFilterExpression(children.get(i)).condition());
    }
    return subQuery;
  }

  private TermFilterExpression<?> termFilterExpression(FilterExpression<?> filterExpression) {
    if (!(filterExpression instanceof TermFilterExpression)) {
      throw new IllegalStateException("Unsupported filter expression");
    }
    return (TermFilterExpression<?>) filterExpression;
  }
}
