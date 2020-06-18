package com.meemaw.shared.rest;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
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
    SelectConnectByStep<?> subQuery = null;
    for (FilterExpression<?> filterExpression : children) {
      if (filterExpression instanceof BooleanFilterExpression) {
        subQuery = ((BooleanFilterExpression) filterExpression).sql(query);
      } else if (filterExpression instanceof TermFilterExpression) {
        subQuery = ((TermFilterExpression<?>) filterExpression).sql(query);
      } else {
        throw new IllegalStateException("Unsupported filter expression");
      }
    }
    return subQuery;
  }
}
