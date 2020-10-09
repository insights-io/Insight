package com.rebrowse.model;

import static com.rebrowse.SameJSON.sameJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rebrowse.BaseRebrowseTest;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.net.ApiResource;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class OrganizationTest extends BaseRebrowseTest {

  @Test
  public void testSerialization() throws IOException, URISyntaxException {
    String payload = readFixture("/organization.json");
    Organization organization = ApiResource.OBJECT_MAPPER.readValue(payload, Organization.class);
    assertNotNull(organization);
    assertNotNull(organization.getId());
    assertThat(payload, sameJson(ApiResource.OBJECT_MAPPER.writeValueAsString(organization)));
  }
}
