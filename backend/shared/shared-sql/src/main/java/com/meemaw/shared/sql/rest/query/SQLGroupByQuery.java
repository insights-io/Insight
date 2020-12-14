package com.meemaw.shared.sql.rest.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meemaw.shared.rest.query.GroupByQuery;
import com.meemaw.shared.rest.query.TimePrecision;
import com.rebrowse.api.RebrowseApi;
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

  private static final String COUNT = "count";

  GroupByQuery groupBy;
  TimePrecision dateTrunc;

  public static SQLGroupByQuery of(GroupByQuery groupBy, TimePrecision dateTrunc) {
    return new SQLGroupByQuery(groupBy, dateTrunc);
  }

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
        .map(
            fieldName -> {
              Field<?> field = SQLFilterExpression.jsonText(fieldName, String.class);
              if (dateTrunc != null) {
                return DatetimeFunctions.dateTrunc(dateTrunc.getKey(), field).as(fieldName);
              }
              return field.as(fieldName);
            });
  }

  public List<Field<?>> count() {
    return Stream.concat(fields(), Stream.of(DSL.count().as(COUNT))).collect(Collectors.toList());
  }

  public JsonNode asJsonNode(RowSet<Row> rows, ObjectMapper objectMapper) {
    List<Field<?>> columns = count();
    if (1 == columns.size()) {
      ObjectNode node = objectMapper.createObjectNode();
      node.put(COUNT, rows.iterator().next().getInteger(COUNT));
      return node;
    }

    ArrayNode results = objectMapper.createArrayNode();
    rows.forEach(
        row -> {
          ObjectNode node = objectMapper.createObjectNode();
          node.put(COUNT, row.getInteger(COUNT));
          for (int i = 0; i < columns.size() - 1; i++) {
            String column = columns.get(i).getName();
            if ("created_at".equals(column)) {
              node.put(
                  column, row.getOffsetDateTime(column).format(RebrowseApi.DATE_TIME_FORMATTER));
            } else {
              node.put(column, row.getString(column));
            }
          }
          results.add(node);
        });

    return results;
  }
}
