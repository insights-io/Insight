package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.shared.rest.query.BooleanOperation;
import lombok.ToString;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;

@ToString
public enum SQLBooleanOperation {
  AND {
    @Override
    public SelectConditionStep<?> apply(SelectConditionStep<?> field, Condition condition) {
      return field.and(condition);
    }
  },
  OR {
    @Override
    public SelectConditionStep<?> apply(SelectConditionStep<?> field, Condition condition) {
      return field.or(condition);
    }
  };

  public abstract SelectConditionStep<?> apply(SelectConditionStep<?> query, Condition condition);

  public static SQLBooleanOperation of(BooleanOperation booleanOperation) {
    return SQLBooleanOperation.valueOf(booleanOperation.name());
  }
}
