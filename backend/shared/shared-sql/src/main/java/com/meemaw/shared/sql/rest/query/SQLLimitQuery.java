package com.meemaw.shared.sql.rest.query;

import lombok.Value;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;

@Value
public class SQLLimitQuery implements QueryPart {

  int limit;

  public static SQLLimitQuery of(int limit) {
    return new SQLLimitQuery(limit);
  }

  @Override
  public SelectForUpdateStep<?> apply(SelectForUpdateStep<?> select) {
    return ((SelectConditionStep<?>) select).limit(limit);
  }
}
