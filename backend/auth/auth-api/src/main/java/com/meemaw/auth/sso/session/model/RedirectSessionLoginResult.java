package com.meemaw.auth.sso.session.model;

import lombok.Value;

import java.net.URL;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Value
public class RedirectSessionLoginResult implements LoginResult<Void> {

  String sessionId;
  URL location;

  @Override
  public Response loginResponse(String cookieDomain) {
    return Response.status(Status.FOUND)
        .header("Location", location)
        .cookie(cookie(cookieDomain))
        .build();
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoSession.cookie(sessionId, cookieDomain);
  }
}
