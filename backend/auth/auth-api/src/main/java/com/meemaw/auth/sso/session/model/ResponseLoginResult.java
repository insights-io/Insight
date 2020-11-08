package com.meemaw.auth.sso.session.model;

import lombok.Value;

import java.util.function.Function;
import javax.ws.rs.core.Response;

@Value
public class ResponseLoginResult implements LoginResult<Void> {

  Function<String, Response> responseSupplier;

  @Override
  public Response loginResponse(String cookieDomain) {
    return responseSupplier.apply(cookieDomain);
  }
}
