package com.meemaw.shared.rest;

import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

public abstract class FilterExpression {

  /**
   * Update query with filter expression.
   *
   * @param query existing select query
   * @return query with applied filter conditions
   */
  public abstract SelectConditionStep<?> sql(SelectJoinStep<?> query);
}
