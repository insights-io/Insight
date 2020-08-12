package com.meemaw.auth.sso.model;

import javax.ws.rs.core.Response;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResult {

  String sessionId;
  String tfaClientId;

  public Response response(String cookieDomain) {
    if (sessionId != null) {
      return SsoSession.cookieResponse(sessionId, cookieDomain);
    } else {
      return TfaClientId.cookieResponse(tfaClientId, cookieDomain);
    }
  }
}
