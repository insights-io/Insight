package com.meemaw.auth.sso.session.model;

import lombok.Value;

import javax.ws.rs.core.Response;

@Value
public class SsoLoginResult<T> {

  LoginResult<T> loginResult;
  String cookieDomain;

  public Response response() {
    return loginResult.loginResponse(cookieDomain);
  }
}
