package com.meemaw.shared.rest.query.sql;

import com.meemaw.shared.rest.query.TermOperation;
import lombok.ToString;
import org.jooq.Condition;
import org.jooq.Field;

@ToString
public enum SQLTermOperation {
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

  public abstract <T> Condition apply(Field<T> field, T target);

  public static SQLTermOperation of(TermOperation termOperation) {
    return SQLTermOperation.valueOf(termOperation.name());
  }
}
