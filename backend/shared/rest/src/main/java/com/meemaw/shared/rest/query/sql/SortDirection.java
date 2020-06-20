package com.meemaw.shared.rest.query.sql;

import static org.jooq.impl.DSL.field;

import lombok.ToString;
import org.jooq.SortField;

@ToString
public enum SortDirection {
  ASC {
    @Override
    public SortField<?> sortField(String field) {
      return field(field).asc();
    }

    @Override
    public char getSymbol() {
      return '+';
    }
  },
  DESC {
    @Override
    public SortField<?> sortField(String field) {
      return field(field).desc();
    }

    @Override
    public char getSymbol() {
      return '-';
    }
  };

  public abstract SortField<?> sortField(String field);

  public abstract char getSymbol();
}
