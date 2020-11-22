package com.meemaw.shared.sql.rest.query;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.rhs.colon.RHSColonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.junit.jupiter.api.Test;

public class RHSColorParserSqlTest {

  @Test
  public void should_correctly_parse_rhs_colon_mixed_query_to_sql() throws MalformedURLException {
    String input =
        "http://www.abc.com?field1=lte:123&field1=gte:100&field2=gte:200&limit=25&location.city=eq:10";

    Set<String> allowedFields = Set.of("field1", "field2", "location.city");
    Map<String, Field<?>> mappings =
        allowedFields.stream().collect(Collectors.toMap(v -> v, v -> field(v, String.class)));

    SearchDTO searchDTO =
        RHSColonParser.parse(RHSColonParser.queryParams(new URL(input)), allowedFields);

    Query query =
        SQLSearchDTO.of(searchDTO).query(select().from(table("session.session")), mappings);

    assertEquals(
        "select * from session.session where (field1 <= '123' and field1 >= '100' and field2 >= '200' and location ->> 'city' = '10') limit 25",
        query.getSQL(ParamType.INLINED));
  }

  @Test
  public void should_correctly_parse_rhs_colon_and_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com?field1=lte:123&sort_by=+field2,-age&field2=gte:matej";

    Set<String> allowedFields = Set.of("field1", "field2", "age");
    Map<String, Field<?>> mappings =
        allowedFields.stream().collect(Collectors.toMap(v -> v, v -> field(v, String.class)));

    SearchDTO searchDTO =
        RHSColonParser.parse(RHSColonParser.queryParams(new URL(input)), allowedFields);

    Query query =
        SQLSearchDTO.of(searchDTO).query(select().from(table("session.session")), mappings);

    assertEquals(
        "select * from session.session where (field1 <= '123' and field2 >= 'matej') order by field2 asc, age desc",
        query.getSQL(ParamType.INLINED));
  }

  @Test
  public void should_correctly_parse_rhs_colon_empty_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com";
    SearchDTO searchDTO =
        RHSColonParser.parse(RHSColonParser.queryParams(new URL(input)), Collections.emptySet());

    Query query =
        SQLSearchDTO.of(searchDTO)
            .query(select().from(table("session.session")), Collections.emptyMap());

    assertEquals("select * from session.session", query.getSQL(ParamType.INLINED));
  }

  @Test
  public void parse__should_handle_sort_directions__when_with_or_without_symbol()
      throws MalformedURLException {
    Set<String> allowedFields = Set.of("created_at");
    Map<String, Field<?>> mappings =
        allowedFields.stream().collect(Collectors.toMap(v -> v, v -> field(v, String.class)));

    assertEquals(
        "select * from session.session order by created_at asc",
        SQLSearchDTO.of(
                RHSColonParser.parse(
                    RHSColonParser.queryParams(new URL("http://www.abc.com?sort_by=+created_at")),
                    allowedFields))
            .query(select().from(table("session.session")), mappings)
            .getSQL(ParamType.INLINED));

    assertEquals(
        "select * from session.session order by created_at asc",
        SQLSearchDTO.of(
                RHSColonParser.parse(
                    RHSColonParser.queryParams(new URL("http://www.abc.com?sort_by=createdAt")),
                    allowedFields))
            .query(select().from(table("session.session")), mappings)
            .getSQL(ParamType.INLINED));

    assertEquals(
        "select * from session.session order by created_at desc",
        SQLSearchDTO.of(
                RHSColonParser.parse(
                    RHSColonParser.queryParams(new URL("http://www.abc.com?sort_by=-created_at")),
                    allowedFields))
            .query(select().from(table("session.session")), mappings)
            .getSQL(ParamType.INLINED));
  }
}
