package com.rebrowse.model;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.session.Session;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class SessionTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/session.json");
    Session session = objectMapper.readValue(payload, Session.class);
    assertNotNull(session);
    assertNotNull(session.getId());
    assertThat(payload, sameJson(objectMapper.writeValueAsString(session)));
  }
}
