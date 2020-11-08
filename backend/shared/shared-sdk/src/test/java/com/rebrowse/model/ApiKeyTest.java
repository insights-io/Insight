package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.auth.ApiKey;
import com.rebrowse.net.ApiResource;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class ApiKeyTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/api_key.json");
    ApiKey apiKey = ApiResource.OBJECT_MAPPER.readValue(payload, ApiKey.class);
    assertNotNull(apiKey);
    assertNotNull(apiKey.getUserId());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(apiKey)));
  }
}
