package com.meemaw.shared.rest;

import lombok.ToString;
import org.jooq.Condition;
import org.jooq.Field;

@ToString
public enum TermOperation {
  EQ {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.eq(target);
    }
  },
  GT {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.gt(target);
    }
  },
  GTE {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.ge(target);
    }
  },
  LT {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.lt(target);
    }
  },
  LTE {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.le(target);
    }
  };

  public abstract <T> Condition sql(Field<T> field, T target);
}
