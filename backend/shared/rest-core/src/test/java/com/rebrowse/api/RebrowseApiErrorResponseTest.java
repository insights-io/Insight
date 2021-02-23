package com.rebrowse.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rebrowse.test.matchers.SameJSON;
import java.io.IOException;
import java.net.URISyntaxException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class RebrowseApiErrorResponseTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/errors/unauthorized.json");
    RebrowseApiErrorResponse<?> errorDataResponse =
        objectMapper.readValue(payload, RebrowseApiErrorResponse.class);

    assertNotNull(errorDataResponse);
    assertNotNull(errorDataResponse.getError());
    assertEquals(401, errorDataResponse.getError().getStatusCode());
    MatcherAssert.assertThat(
        payload, SameJSON.sameJson(objectMapper.writeValueAsString(errorDataResponse)));
  }
}
