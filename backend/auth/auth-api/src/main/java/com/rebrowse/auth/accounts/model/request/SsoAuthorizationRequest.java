package com.rebrowse.auth.accounts.model.request;

import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import java.net.URI;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class SsoAuthorizationRequest implements AuthorizationResponse {

  URI location;
  String domain;
  String state;

  @Override
  public Response response(NewCookie... cookies) {
    return Response.status(Response.Status.FOUND)
        .location(location)
        .cookie(SsoAuthorizationSession.cookie(state, domain))
        .build();
  }
}
