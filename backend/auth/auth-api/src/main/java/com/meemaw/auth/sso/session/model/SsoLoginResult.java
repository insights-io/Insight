package com.meemaw.auth.sso.session.model;

import com.meemaw.auth.sso.SsoSignInSession;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class SsoLoginResult<T> {

  LoginResult<T> loginResult;
  String cookieDomain;

  // TODO: write test that sso session is cleared
  public Response response() {
    return loginResult
        .loginResponseBuilder(cookieDomain)
        .cookie(loginResult.cookie(cookieDomain), SsoSignInSession.clearCookie(cookieDomain))
        .build();
  }
}
