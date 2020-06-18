package com.meemaw.shared.rest;

import static org.jooq.impl.DSL.field;

import lombok.ToString;
import org.jooq.SortField;

@ToString
public enum SortDirection {
  ASC("ASC") {
    @Override
    public SortField<?> sortField(String field) {
      return field(field).asc();
    }
  },
  DESC("DESC") {
    @Override
    public SortField<?> sortField(String field) {
      return field(field).desc();
    }
  };

  private final String direction;

  SortDirection(String direction) {
    this.direction = direction;
  }

  public abstract SortField<?> sortField(String field);
}
