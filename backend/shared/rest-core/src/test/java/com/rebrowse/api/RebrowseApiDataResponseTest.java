package com.rebrowse.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.test.matchers.SameJSON;
import java.io.IOException;
import java.net.URISyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RebrowseApiDataResponseTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/data_response.json");
    RebrowseApiDataResponse<Count> dataResponse =
        objectMapper.readValue(payload, new TypeReference<>() {});

    Assertions.assertNotNull(dataResponse);
    Assertions.assertEquals(10, dataResponse.getData().getCount());

    MatcherAssert.assertThat(
        payload, SameJSON.sameJson(objectMapper.writeValueAsString(dataResponse)));
  }

  @Value
  @NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
  private static class Count {
    int count;
  }
}
