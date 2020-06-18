package com.meemaw.shared.rest;

import java.util.List;
import lombok.Data;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

@Data
public abstract class FilterExpression {

  List<FilterExpression> children;

  /**
   * Update query with filter expression.
   *
   * @param query existing select query
   * @return query with applied filter conditions
   */
  public abstract SelectConditionStep<?> sql(SelectJoinStep<?> query);
}
