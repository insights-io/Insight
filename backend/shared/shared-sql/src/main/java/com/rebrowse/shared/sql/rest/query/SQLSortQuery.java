package com.rebrowse.shared.sql.rest.query;

import com.rebrowse.shared.rest.query.SortQuery;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.SortField;

@Value
public class SQLSortQuery implements QueryPart {

  SortQuery sortQuery;

  public static SQLSortQuery of(SortQuery sortQuery) {
    return new SQLSortQuery(sortQuery);
  }

  @Override
  public SelectForUpdateStep<?> apply(SelectForUpdateStep<?> select) {
    return ((SelectConditionStep<?>) select).orderBy(sortByFields());
  }

  private List<SortField<?>> sortByFields() {
    return sortQuery.getOrders().stream()
        .map(order -> SQLSortDirection.of(order.getRight()).apply(order.getLeft()))
        .collect(Collectors.toList());
  }
}
