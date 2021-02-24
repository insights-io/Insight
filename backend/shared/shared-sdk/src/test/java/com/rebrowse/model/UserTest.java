package com.rebrowse.model;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.user.User;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class UserTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/user.json");
    User user = objectMapper.readValue(payload, User.class);
    assertNotNull(user);
    assertNotNull(user.getId());
    assertThat(payload, sameJson(objectMapper.writeValueAsString(user)));
  }
}
