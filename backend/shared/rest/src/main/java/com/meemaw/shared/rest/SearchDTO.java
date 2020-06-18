package com.meemaw.shared.rest;

import lombok.Value;
import org.jooq.Query;
import org.jooq.SelectSeekStepN;

@Value
public class SearchDTO<P extends Query> {

  FilterExpression<P> filter;
  SortQuery sort;

  public SelectSeekStepN<?> sql(P query) {
    return filter.sql(query).orderBy(sort.sql());
  }
}
