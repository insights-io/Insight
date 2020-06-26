package com.meemaw.shared.rest.query.sql;

import static org.jooq.impl.DSL.field;

import com.meemaw.shared.rest.query.SortDirection;
import lombok.ToString;
import org.jooq.SortField;

@ToString
public enum SQLSortDirection {
  ASC {
    @Override
    public SortField<?> apply(String field) {
      return field(field).asc();
    }
  },
  DESC {
    @Override
    public SortField<?> apply(String field) {
      return field(field).desc();
    }
  };

  public abstract SortField<?> apply(String field);

  public static SQLSortDirection of(SortDirection sortDirection) {
    return SQLSortDirection.valueOf(sortDirection.name());
  }
}
