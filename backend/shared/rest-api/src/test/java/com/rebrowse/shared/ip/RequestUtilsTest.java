package com.rebrowse.shared.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.shared.SharedConstants;
import com.rebrowse.shared.context.URIUtils;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RequestUtilsTest {

  @Test
  public void parse_base_url__should_handle_different_urls() {
    URI withPort = URI.create("http://localhost:3000/login?redirect=%2F");
    assertEquals("http://localhost:3000", URIUtils.parseOrigin(withPort).toString());

    URI withNoPort = URI.create("https://google.com/login?redirect=%2F");
    assertEquals("https://google.com", URIUtils.parseOrigin(withNoPort).toString());

    URI withTrailingSlash = URI.create("http://localhost:3000/");
    assertEquals("http://localhost:3000", URIUtils.parseOrigin(withTrailingSlash).toString());

    URI withNoTrailingSlash = URI.create("http://localhost:3000/more");
    assertEquals("http://localhost:3000", URIUtils.parseOrigin(withNoTrailingSlash).toString());
  }

  @Test
  public void parse_tld__should_handle_different_urls() {
    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        URIUtils.parseTopLevelDomain(
            URI.create(String.format("http://app.%s", SharedConstants.REBROWSE_STAGING_DOMAIN))));

    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        URIUtils.parseTopLevelDomain(
            URI.create(String.format("https://app.%s", SharedConstants.REBROWSE_STAGING_DOMAIN))));
    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        URIUtils.parseTopLevelDomain(
            URI.create(String.format("https://%s", SharedConstants.REBROWSE_STAGING_DOMAIN))));

    assertEquals(Optional.empty(), URIUtils.parseTopLevelDomain(URI.create("app")));
    assertEquals(
        Optional.empty(), URIUtils.parseTopLevelDomain(URI.create("http://localhost:3000")));
    assertEquals(Optional.empty(), URIUtils.parseTopLevelDomain(URI.create("")));
  }
}
