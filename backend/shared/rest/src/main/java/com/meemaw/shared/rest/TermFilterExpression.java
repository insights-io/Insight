package com.meemaw.shared.rest;

import static org.jooq.impl.DSL.field;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;

@Value
@EqualsAndHashCode(callSuper = true)
public class TermFilterExpression<T> extends FilterExpression {

  public String field;
  public TermOperation operation;
  public T target;

  public Condition condition() {
    return operation.sql(field(field), target);
  }

  @Override
  public SelectConditionStep<?> sql(SelectJoinStep<?> query) {
    return query.where(condition());
  }
}
