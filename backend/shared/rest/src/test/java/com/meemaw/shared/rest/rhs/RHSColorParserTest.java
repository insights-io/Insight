package com.meemaw.shared.rest.rhs;

import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.shared.rest.SearchDTO;
import com.meemaw.shared.rest.rhs.colon.RHSColonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jooq.Query;
import org.jooq.SelectJoinStep;
import org.junit.jupiter.api.Test;

public class RHSColorParserTest {

  @Test
  public void should_correctly_parse_complex_rhs_colon_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com?field1=lte:123&sort_by=+field2,-age&field2=gte:matej";
    Map<String, List<String>> params = RHSColonParser.queryParams(new URL(input));
    SearchDTO<SelectJoinStep<?>> searchDTO = RHSColonParser.buildFromParams(params);

    Query query = searchDTO.sql(select().from(table("session.session")));
    assertEquals(
        "select * from session.session where (field1 <= ? and field2 >= ?) order by field2 asc, age desc",
        query.getSQL());
    assertEquals(List.of("123", "matej"), query.getBindValues());
  }

  @Test
  public void should_correctly_parse_empty_rhs_colon_query_to_sql() throws MalformedURLException {
    String input = "http://www.abc.com";
    Map<String, List<String>> params = RHSColonParser.queryParams(new URL(input));
    SearchDTO<SelectJoinStep<?>> searchDTO = RHSColonParser.buildFromParams(params);

    Query query = searchDTO.sql(select().from(table("session.session")));
    assertEquals("select * from session.session", query.getSQL());
    assertEquals(Collections.emptyList(), query.getBindValues());
  }
}
