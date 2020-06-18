package com.meemaw.shared.rest;

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
  },
  DESC {
    @Override
    public SortField<?> sortField(String field) {
      return field(field).desc();
    }
  };

  public abstract SortField<?> sortField(String field);
}
