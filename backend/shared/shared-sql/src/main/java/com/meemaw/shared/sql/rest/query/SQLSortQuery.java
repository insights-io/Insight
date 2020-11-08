package com.meemaw.shared.sql.rest.query;

import lombok.Value;
import org.jooq.SortField;

import com.meemaw.shared.rest.query.SortQuery;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class SQLSortQuery {

  SortQuery sortQuery;

  /**
   * Returns list of sort SQL sort fields.
   *
   * @return list of sort fields
   */
  public List<SortField<?>> apply() {
    return sortQuery.getOrders().stream()
        .map(order -> SQLSortDirection.of(order.getRight()).apply(order.getLeft()))
        .collect(Collectors.toList());
  }

  public static SQLSortQuery of(SortQuery sortQuery) {
    return new SQLSortQuery(sortQuery);
  }
}
