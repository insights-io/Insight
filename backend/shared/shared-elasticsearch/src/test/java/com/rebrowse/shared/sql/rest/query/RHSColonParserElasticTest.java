package com.rebrowse.shared.sql.rest.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.shared.elasticsearch.rest.query.ElasticSearchDTO;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.query.rhs.colon.RHSColonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

public class RHSColonParserElasticTest {

  @Test
  public void should_correctly_parse_rhs_colon_query_to_elastic() throws MalformedURLException {
    String input = "http://www.abc.com?field1=lte:123&field1=gte:10&limit=200&field2.t=eq:aba";
    SearchDTO searchDTO =
        RHSColonParser.parse(
            RHSColonParser.queryParams(new URL(input)), Set.of("field1", "field2.t"));

    SearchSourceBuilder query = ElasticSearchDTO.of(searchDTO).apply();
    assertEquals(
        "{\"size\":200,\"query\":{\"bool\":{\"filter\":[{\"bool\":{\"filter\":[{\"range\":{\"field1\":{\"from\":null,\"to\":\"123\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},{\"range\":{\"field1\":{\"from\":\"10\",\"to\":null,\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},{\"bool\":{\"filter\":[{\"nested\":{\"query\":{\"term\":{\"field2.t\":{\"value\":\"aba\",\"boost\":1.0}}},\"path\":\"field2\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}",
        query.toString());
  }

  @Test
  public void should_correctly_parse_rhs_colon_empty_query_to_elastic()
      throws MalformedURLException {
    String input = "http://www.abc.com";
    SearchDTO searchDTO =
        RHSColonParser.parse(RHSColonParser.queryParams(new URL(input)), Collections.emptySet());

    SearchSourceBuilder query = ElasticSearchDTO.of(searchDTO).apply();
    assertEquals(
        "{\"query\":{\"bool\":{\"filter\":[{\"match_all\":{\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}",
        query.toString());
  }
}
