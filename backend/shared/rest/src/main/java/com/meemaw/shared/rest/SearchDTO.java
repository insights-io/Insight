package com.meemaw.shared.rest;

import lombok.Value;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStepN;

@Value
public class SearchDTO {

  FilterExpression filter;
  SortQuery sort;

  public SelectSeekStepN<?> sql(SelectJoinStep<?> query) {
    return filter.sql(query).orderBy(sort.sql());
  }
}
