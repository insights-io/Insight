package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.sso.session.model.SsoSession;
import java.net.URI;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;

/**
 * Redirects user directly to the callback redirect with SsoSession cookie. Use on end of the
 * request flows that are triggered server side, e.g. "Sign up success", "Team invite success"
 */
@Value
public class DirectAuthorizationRedirectResponse implements AuthorizationResponse {

  URI location;
  String domain;
  String sessionId;

  @Override
  public Response response(NewCookie... cookies) {
    return Response.status(Response.Status.FOUND)
        .location(location)
        .cookie(SsoSession.cookie(sessionId, domain))
        .build();
  }
}
