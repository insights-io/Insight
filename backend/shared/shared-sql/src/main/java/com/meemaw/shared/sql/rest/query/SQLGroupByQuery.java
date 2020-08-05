package com.meemaw.shared.sql.rest.query;

import com.meemaw.shared.rest.query.GroupByQuery;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import org.jooq.Field;
import org.jooq.impl.DSL;

@Value
public class SQLGroupByQuery {

  GroupByQuery groupBy;

  /**
   * Returns list of sort SQL sort fields.
   *
   * @return list of sort fields
   */
  public List<Field<?>> apply() {
    return fields().collect(Collectors.toList());
  }

  private Stream<Field<?>> fields() {
    return groupBy.getFields().stream().map(field -> SQLFilterExpression.field(field).as(field));
  }

  public List<Field<?>> fieldsWithCount() {
    return Stream.concat(fields(), Stream.of(DSL.count())).collect(Collectors.toList());
  }

  public static SQLGroupByQuery of(GroupByQuery groupBy) {
    return new SQLGroupByQuery(groupBy);
  }
}
