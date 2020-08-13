package com.meemaw.auth.sso.model;

import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class DirectLoginResult implements LoginResult {

  String sessionId;

  @Override
  public Response response(String cookieDomain) {
    return SsoSession.cookieResponse(sessionId, cookieDomain);
  }
}
