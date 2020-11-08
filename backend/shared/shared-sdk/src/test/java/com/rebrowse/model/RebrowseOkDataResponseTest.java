package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class RebrowseOkDataResponseTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/user_data_response.json");
    RebrowseOkDataResponse<User> userDataResponse =
        ApiResource.OBJECT_MAPPER.readValue(payload, new TypeReference<>() {});

    assertNotNull(userDataResponse);
    assertNotNull(userDataResponse.getData());
    assertNotNull(userDataResponse.getData().getId());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(userDataResponse)));
  }
}
