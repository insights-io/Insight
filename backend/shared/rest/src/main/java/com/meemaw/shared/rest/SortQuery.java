package com.meemaw.shared.rest;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.SortField;

@Value
public class SortQuery {

  public List<Pair<String, SortDirection>> orders;

  public List<SortField<?>> sql() {
    return orders.stream()
        .map(order -> order.getRight().sortField(order.getLeft()))
        .collect(Collectors.toList());
  }
}
