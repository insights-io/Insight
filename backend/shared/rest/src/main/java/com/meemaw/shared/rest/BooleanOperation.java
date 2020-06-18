package com.meemaw.shared.rest;

import lombok.Getter;
import lombok.ToString;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;

@ToString
public enum BooleanOperation {
  AND("AND") {
    @Override
    public SelectConditionStep<?> applyCondition(
        SelectConditionStep<?> field, Condition condition) {
      return field.and(condition);
    }
  },
  OR("OR") {
    @Override
    public SelectConditionStep<?> applyCondition(
        SelectConditionStep<?> field, Condition condition) {
      return field.or(condition);
    }
  };

  @Getter private final String text;

  BooleanOperation(String text) {
    this.text = text;
  }

  public abstract SelectConditionStep<?> applyCondition(
      SelectConditionStep<?> query, Condition condition);
}
