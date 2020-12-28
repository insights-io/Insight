package com.meemaw.shared.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.shared.SharedConstants;
import com.meemaw.shared.context.RequestUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RequestUtilsTest {

  @Test
  public void parse_base_url__should_handle_different_urls() throws MalformedURLException {
    URL withPort = new URL("http://localhost:3000/login?redirect=%2F");
    assertEquals("http://localhost:3000", RequestUtils.parseOrigin(withPort).toString());

    URL withNoPort = new URL("https://google.com/login?redirect=%2F");
    assertEquals("https://google.com", RequestUtils.parseOrigin(withNoPort).toString());

    URL withTrailingSlash = new URL("http://localhost:3000/");
    assertEquals("http://localhost:3000", RequestUtils.parseOrigin(withTrailingSlash).toString());

    URL withNoTrailingSlash = new URL("http://localhost:3000/more");
    assertEquals("http://localhost:3000", RequestUtils.parseOrigin(withNoTrailingSlash).toString());
  }

  @Test
  public void parse_tld__should_handle_different_urls() {
    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        RequestUtils.parseTopLevelDomain(
            String.format("http://app.%s", SharedConstants.REBROWSE_STAGING_DOMAIN)));

    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        RequestUtils.parseTopLevelDomain(
            String.format("https://app.%s", SharedConstants.REBROWSE_STAGING_DOMAIN)));
    assertEquals(
        Optional.of(SharedConstants.REBROWSE_STAGING_DOMAIN),
        RequestUtils.parseTopLevelDomain(
            String.format("https://%s", SharedConstants.REBROWSE_STAGING_DOMAIN)));
    assertEquals(Optional.empty(), RequestUtils.parseTopLevelDomain("app"));
    assertEquals(Optional.empty(), RequestUtils.parseTopLevelDomain("http://localhost:3000"));
    assertEquals(Optional.empty(), RequestUtils.parseTopLevelDomain(""));
  }
}
