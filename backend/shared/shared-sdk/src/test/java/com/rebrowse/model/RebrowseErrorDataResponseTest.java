package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.error.RebrowseErrorDataResponse;
import com.rebrowse.net.ApiResource;

import java.io.IOException;
import java.net.URISyntaxException;

public class RebrowseErrorDataResponseTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/errors/unauthorized.json");
    RebrowseErrorDataResponse<?> errorDataResponse =
        ApiResource.OBJECT_MAPPER.readValue(payload, RebrowseErrorDataResponse.class);
    assertNotNull(errorDataResponse);
    assertNotNull(errorDataResponse.getError());
    assertEquals(401, errorDataResponse.getError().getStatusCode());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(errorDataResponse)));
  }
}
