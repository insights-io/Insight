package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.api.query.TermCondition;
import lombok.ToString;
import org.jooq.Condition;
import org.jooq.Field;

@ToString
public enum SQLTermCondition {
  EQ {
    @Override
    public <T> Condition apply(Field<T> field, T target) {
      return field.eq(target);
    }
  },
  GT {
    @Override
    public <T> Condition apply(Field<T> field, T target) {
      return field.gt(target);
    }
  },
  GTE {
    @Override
    public <T> Condition apply(Field<T> field, T target) {
      return field.ge(target);
    }
  },
  LT {
    @Override
    public <T> Condition apply(Field<T> field, T target) {
      return field.lt(target);
    }
  },
  LTE {
    @Override
    public <T> Condition apply(Field<T> field, T target) {
      return field.le(target);
    }
  };

  public static SQLTermCondition of(TermCondition termCondition) {
    return SQLTermCondition.valueOf(termCondition.name());
  }

  public abstract <T> Condition apply(Field<T> field, T target);
}
