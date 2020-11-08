package com.meemaw.auth.sso.session.model;

import lombok.Value;

import javax.ws.rs.core.NewCookie;

@Value
public class DirectLoginResult implements LoginResult<Boolean> {

  String sessionId;

  @Override
  public Boolean getData() {
    return true;
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoSession.cookie(sessionId, cookieDomain);
  }
}
