package com.meemaw.auth.sso.session.model;

import java.util.function.Function;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class ResponseLoginResult implements LoginResult<Void> {

  Function<String, Response.ResponseBuilder> responseSupplier;

  @Override
  public Response.ResponseBuilder loginResponseBuilder(String cookieDomain) {
    return responseSupplier.apply(cookieDomain);
  }
}
