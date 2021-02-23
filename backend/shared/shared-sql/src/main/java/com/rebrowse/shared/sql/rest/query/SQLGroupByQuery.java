package com.rebrowse.shared.sql.rest.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rebrowse.shared.rest.query.GroupByQuery;
import com.rebrowse.shared.rest.query.TimePrecision;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.shared.rest.query.AbstractQueryParser;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.impl.DSL;

@Value
public class SQLGroupByQuery implements QueryPart {

  GroupByQuery groupBy;
  TimePrecision datePart;

  public static SQLGroupByQuery of(GroupByQuery groupBy, TimePrecision datePart) {
    return new SQLGroupByQuery(groupBy, datePart);
  }

  public static JsonNode mapRowsToJsonNode(
      RowSet<Row> rows, List<Field<?>> columns, ObjectMapper objectMapper) {
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
            Object value = row.getValue(column);

            if (value == null) {
              node.put(column, (String) null);
            } else if (value instanceof OffsetDateTime) {
              node.put(column, ((OffsetDateTime) value).format(RebrowseApi.DATE_TIME_FORMATTER));
            } else {
              node.put(column, value.toString());
            }
          }
          results.add(node);
        });

    return results;
  }

  @Override
  public SelectForUpdateStep<?> apply(SelectForUpdateStep<?> select) {
    return ((SelectConditionStep<?>) select)
        .groupBy(groupBy.getFields().stream().map(this::field).collect(Collectors.toList()));
  }

  public List<Field<?>> selectFieldsWithCount() {
    return Stream.concat(
            groupBy.getFields().stream().map(fieldName -> field(fieldName).as(fieldName)),
            Stream.of(DSL.count().as("count")))
        .collect(Collectors.toList());
  }

  private Field<?> field(String fieldName) {
    Field<?> field = SQLFilterExpression.jsonText(AbstractQueryParser.snakeCase(fieldName), String.class);
    if (datePart != null) {
      return DatetimeFunctions.dateTrunc(field, datePart);
    }
    return field;
  }
}
