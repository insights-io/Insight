package com.meemaw.auth.sso.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SsoSessionTest {

  @Test
  public void ssoSessionIdentifier_should_be_50_charactersLong() {
    assertEquals(50, SsoSession.newIdentifier().length());
  }

  @Test
  public void ssoSessionCookie_should_be_correctly_named() {
    String sessionId = SsoSession.newIdentifier();
    assertEquals(SsoSession.COOKIE_NAME, SsoSession.cookie(sessionId).getName());
  }
}
