package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.session.Session;
import com.rebrowse.net.ApiResource;

import java.io.IOException;
import java.net.URISyntaxException;

public class SessionTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/session.json");
    Session session = ApiResource.OBJECT_MAPPER.readValue(payload, Session.class);
    assertNotNull(session);
    assertNotNull(session.getId());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(session)));
  }
}
