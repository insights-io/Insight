package com.rebrowse.auth.sso.session.model;

import java.util.function.Function;
import javax.ws.rs.core.Response.ResponseBuilder;
import lombok.Value;

@Value
public class ResponseLoginResult implements LoginResult<Void> {

  Function<String, ResponseBuilder> responseSupplier;

  @Override
  public ResponseBuilder loginResponseBuilder(String domain) {
    return responseSupplier.apply(domain);
  }
}
