package com.meemaw.shared.rest.rhs;

import static com.meemaw.test.matchers.SameJSON.assertEquals;

import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.elasticsearch.ElasticSearchDTO;
import com.meemaw.shared.rest.query.rhs.colon.RHSColonParser;
import java.net.MalformedURLException;
import java.net.URL;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class RHSColonParserElasticTest {

  @Test
  public void should_correctly_parse_rhs_colon_query_to_sql()
      throws MalformedURLException, JSONException {
    String input = "http://www.abc.com?field1=lte:123&limit=200&field2.t=eq:aba";
    SearchDTO searchDTO =
        RHSColonParser.buildFromParams(RHSColonParser.queryParams(new URL(input)));

    SearchSourceBuilder query = ElasticSearchDTO.of(searchDTO).apply();
    System.out.println(query.toString());
    assertEquals(
        "{\"size\":200,\"query\":{\"bool\":{\"filter\":[{\"bool\":{\"filter\":[{\"range\":{\"field1\":{\"from\":null,\"to\":\"123\",\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},{\"bool\":{\"filter\":[{\"nested\":{\"query\":{\"term\":{\"field2.t\":{\"value\":\"aba\",\"boost\":1.0}}},\"path\":\"field2\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}\n",
        query.toString());
  }

  @Test
  public void should_correctly_parse_rhs_colon_empty_query_to_elastic()
      throws MalformedURLException, JSONException {
    String input = "http://www.abc.com";
    SearchDTO searchDTO =
        RHSColonParser.buildFromParams(RHSColonParser.queryParams(new URL(input)));

    SearchSourceBuilder query = ElasticSearchDTO.of(searchDTO).apply();
    assertEquals(
        "{\"query\":{\"bool\":{\"filter\":[{\"match_all\":{\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}}}",
        query.toString());
  }
}
