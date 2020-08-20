package com.meemaw.shared.sql.rest.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.shared.rest.query.GroupByQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
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
    return groupBy.getFields().stream()
        .map(field -> SQLFilterExpression.sqlFilterField(field, String.class).as(field));
  }

  public List<Field<?>> fieldsWithCount() {
    return Stream.concat(fields(), Stream.of(DSL.count())).collect(Collectors.toList());
  }

  public static SQLGroupByQuery of(GroupByQuery groupBy) {
    return new SQLGroupByQuery(groupBy);
  }

  public JsonNode asJsonNode(RowSet<Row> rows, ObjectMapper objectMapper) {
    List<Field<?>> columns = fieldsWithCount();
    if (1 == columns.size()) {
      ObjectNode node = objectMapper.createObjectNode();
      node.put("count", rows.iterator().next().getInteger("count"));
      return node;
    }

    ArrayNode results = objectMapper.createArrayNode();
    rows.forEach(
        row -> {
          ObjectNode node = objectMapper.createObjectNode();
          node.put("count", row.getInteger("count"));
          for (int i = 0; i < columns.size() - 1; i++) {
            String column = columns.get(i).getName();
            node.put(column, row.getString(column));
          }
          results.add(node);
        });

    return results;
  }
}
