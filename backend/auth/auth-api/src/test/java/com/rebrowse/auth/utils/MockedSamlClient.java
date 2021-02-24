package com.rebrowse.auth.utils;

import com.rebrowse.auth.sso.saml.client.SamlClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.enterprise.inject.Alternative;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@Alternative
public class MockedSamlClient extends SamlClient {

  String metadataFileName;

  public static SamlClient okta() {
    return new MockedSamlClient("/sso/saml/metadata/okta_metadata.xml");
  }

  @Override
  public InputStream fetchMetadata(URL metadataEndpoint) throws IOException {
    try {
      return Files.newInputStream(Path.of(getClass().getResource(metadataFileName).toURI()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
