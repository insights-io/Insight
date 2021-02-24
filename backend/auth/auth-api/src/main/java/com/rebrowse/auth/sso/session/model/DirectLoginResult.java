package com.rebrowse.auth.sso.session.model;

import javax.ws.rs.core.NewCookie;
import lombok.Value;

@Value
public class DirectLoginResult implements LoginResult<Boolean> {

  String sessionId;

  @Override
  public Boolean getData() {
    return true;
  }

  @Override
  public NewCookie loginCookie(String cookieDomain) {
    return SsoSession.cookie(sessionId, cookieDomain);
  }
}
