package com.meemaw.shared.rest;

import lombok.Getter;
import lombok.ToString;
import org.jooq.Condition;
import org.jooq.Field;

@ToString
public enum TermOperation {
  EQ("EQ") {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.eq(target);
    }
  },
  GT("GT") {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.gt(target);
    }
  },
  GTE("GTE") {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.ge(target);
    }
  },
  LT("LT") {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.lt(target);
    }
  },
  LTE("LTE") {
    @Override
    public <T> Condition sql(Field<T> field, T target) {
      return field.le(target);
    }
  };

  @Getter private final String text;

  TermOperation(String text) {
    this.text = text;
  }

  public abstract <T> Condition sql(Field<T> field, T target);
}
