package com.rebrowse.auth.sso.session.model;

import java.net.URI;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Value;

@Value
public class RedirectSessionLoginResult implements LoginResult<Void> {

  String sessionId;
  URI location;

  @Override
  public Response.ResponseBuilder loginResponseBuilder(String cookieDomain) {
    return Response.status(Status.FOUND).location(location).cookie(loginCookie(cookieDomain));
  }

  @Override
  public NewCookie loginCookie(String cookieDomain) {
    return SsoSession.cookie(sessionId, cookieDomain);
  }
}
