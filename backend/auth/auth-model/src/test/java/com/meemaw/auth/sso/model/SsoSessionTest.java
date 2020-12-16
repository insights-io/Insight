package com.meemaw.auth.sso.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.sso.session.model.SsoSession;
import org.junit.jupiter.api.Test;

public class SsoSessionTest {

  @Test
  public void sso_session_identifier__should_be_50_characters_long() {
    assertEquals(50, SsoSession.newIdentifier().length());
  }

  @Test
  public void sso_session_cookie__should_be_correctly_named() {
    String sessionId = SsoSession.newIdentifier();
    assertEquals(SsoSession.COOKIE_NAME, SsoSession.cookie(sessionId, null).getName());
  }
}
