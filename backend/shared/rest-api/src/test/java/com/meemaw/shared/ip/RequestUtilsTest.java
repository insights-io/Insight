package com.meemaw.shared.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.shared.context.RequestUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class RequestUtilsTest {

  @Test
  public void should_handle_different_urls() throws MalformedURLException {
    URL withPort = new URL("http://localhost:3000/login?redirect=%2F");
    assertEquals("http://localhost:3000", RequestUtils.parseBaseURL(withPort));

    URL withNoPort = new URL("https://google.com/login?redirect=%2F");
    assertEquals("https://google.com", RequestUtils.parseBaseURL(withNoPort));
  }

  @Test
  public void should_parse_tld() {
    assertEquals(Optional.of("rebrowse.dev"), RequestUtils.parseTLD("http://app.rebrowse.dev"));
    assertEquals(Optional.of("rebrowse.dev"), RequestUtils.parseTLD("https://app.rebrowse.dev"));
    assertEquals(Optional.of("rebrowse.dev"), RequestUtils.parseTLD("https://rebrowse.dev"));
    assertEquals(Optional.empty(), RequestUtils.parseTLD("app"));
    assertEquals(Optional.empty(), RequestUtils.parseTLD("http://localhost:3000"));
    assertEquals(Optional.empty(), RequestUtils.parseTLD(""));
  }

  @Test
  public void should_parse_base_url() throws MalformedURLException {
    assertEquals(
        "http://localhost:3000", RequestUtils.parseBaseURL(new URL("http://localhost:3000/")));
    assertEquals(
        "http://localhost:3000", RequestUtils.parseBaseURL(new URL("http://localhost:3000/more")));
  }
}
