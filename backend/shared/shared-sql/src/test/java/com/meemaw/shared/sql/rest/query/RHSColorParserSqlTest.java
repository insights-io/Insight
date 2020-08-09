package com.meemaw.shared.sql.rest.query;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.rhs.colon.RHSColonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Field;
import org.jooq.Query;
import org.junit.jupiter.api.Test;

public class RHSColorParserSqlTest {

  private static final List<Field<?>> FIELDS = List.of(field("field1"), field("field2"));
  private static final Map<String, Field<?>> FIELD_MAPPINGS =
      FIELDS.stream().collect(Collectors.toMap(Field::getName, field -> field));

  @Test
  public void should_correctly_parse_rhs_colon_mixed_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com?field1=lte:123&field1=gte:100&field2=gte:200&limit=25";
    SearchDTO searchDTO =
        RHSColonParser.parse(
            RHSColonParser.queryParams(new URL(input)), Set.of("field1", "field2"));

    Query query =
        SQLSearchDTO.of(searchDTO).apply(select().from(table("session.session")), FIELD_MAPPINGS);
    assertEquals(
        "select * from session.session where ((field1 <= ? or field1 >= ?) and field2 >= ?) limit ?",
        query.getSQL());

    assertEquals(
        Arrays.toString(List.of("123", "100", "200", "25").toArray()),
        Arrays.toString(query.getBindValues().toArray()));
  }

  @Test
  public void should_correctly_parse_rhs_colon_and_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com?field1=lte:123&sort_by=+field2,-age&field2=gte:matej";
    SearchDTO searchDTO =
        RHSColonParser.parse(
            RHSColonParser.queryParams(new URL(input)), Set.of("field1", "field2"));

    Query query =
        SQLSearchDTO.of(searchDTO).apply(select().from(table("session.session")), FIELD_MAPPINGS);
    assertEquals(
        "select * from session.session where (field1 <= ? and field2 >= ?) order by field2 asc, age desc",
        query.getSQL());
    assertEquals(List.of("123", "matej"), query.getBindValues());
  }

  @Test
  public void should_correctly_parse_rhs_colon_empty_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com";
    SearchDTO searchDTO =
        RHSColonParser.parse(RHSColonParser.queryParams(new URL(input)), Collections.emptySet());

    Query query =
        SQLSearchDTO.of(searchDTO).apply(select().from(table("session.session")), FIELD_MAPPINGS);
    assertEquals("select * from session.session", query.getSQL());
    assertEquals(Collections.emptyList(), query.getBindValues());
  }
}
