package com.meemaw.auth.sso.session.model;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Value;

@Value
public class RedirectLoginResult implements LoginResult<Void> {

  String location;

  @Override
  public Response loginResponse(String cookieDomain) {
    return Response.status(Status.FOUND).header("Location", location).build();
  }
}
