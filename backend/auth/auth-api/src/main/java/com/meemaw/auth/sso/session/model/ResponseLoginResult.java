package com.meemaw.auth.sso.session.model;

import java.util.function.Supplier;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class ResponseLoginResult implements LoginResult<Void> {

  Supplier<Response> responseSupplier;

  @Override
  public Response loginResponse(String cookieDomain) {
    return responseSupplier.get();
  }
}
