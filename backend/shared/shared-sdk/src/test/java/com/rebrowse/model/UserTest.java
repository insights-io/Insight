package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;

import java.io.IOException;
import java.net.URISyntaxException;

public class UserTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/user.json");
    User user = ApiResource.OBJECT_MAPPER.readValue(payload, User.class);
    assertNotNull(user);
    assertNotNull(user.getId());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(user)));
  }
}
