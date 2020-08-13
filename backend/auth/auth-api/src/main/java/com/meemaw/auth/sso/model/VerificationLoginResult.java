package com.meemaw.auth.sso.model;

import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class VerificationLoginResult implements LoginResult {

  String verificationId;

  @Override
  public Response response(String cookieDomain) {
    return SsoVerification.cookieResponse(verificationId, cookieDomain);
  }
}
