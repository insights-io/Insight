package com.meemaw.shared.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class RequestUtilsTest {

  @Test
  public void should_handle_different_urls() throws MalformedURLException {
    URL withPort = new URL("http://localhost:3000/login?dest=%2F");
    assertEquals("http://localhost:3000", RequestUtils.parseBaseURL(withPort));

    URL withNoPort = new URL("https://google.com/login?dest=%2F");
    assertEquals("https://google.com", RequestUtils.parseBaseURL(withNoPort));
  }
}
