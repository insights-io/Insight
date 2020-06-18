package com.meemaw.shared.rest;

import java.util.List;
import lombok.Data;
import org.jooq.Query;
import org.jooq.SelectConnectByStep;

@Data
public abstract class FilterExpression<P extends Query> {
  List<FilterExpression<?>> children;

  public abstract SelectConnectByStep<?> sql(P query);
}
