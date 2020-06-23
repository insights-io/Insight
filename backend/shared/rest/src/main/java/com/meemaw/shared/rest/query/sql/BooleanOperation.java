package com.meemaw.shared.rest.query.sql;

import lombok.ToString;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;

@ToString
public enum BooleanOperation {
  AND {
    @Override
    public SelectConditionStep<?> applyCondition(
        SelectConditionStep<?> field, Condition condition) {
      return field.and(condition);
    }
  },
  OR {
    @Override
    public SelectConditionStep<?> applyCondition(
        SelectConditionStep<?> field, Condition condition) {
      return field.or(condition);
    }
  };

  public abstract SelectConditionStep<?> applyCondition(
      SelectConditionStep<?> query, Condition condition);
}
