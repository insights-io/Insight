package com.rebrowse.auth.sso.saml.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SamlClient {

  // TODO: fetch it async
  public InputStream fetchMetadata(URL metadataEndpoint) throws IOException {
    return metadataEndpoint.openStream();
  }
}
