package com.rebrowse.auth.sso.session.model;

import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class SsoLoginResult<T> {

  LoginResult<T> loginResult;
  String cookieDomain;

  public Response response() {
    return loginResult
        .loginResponseBuilder(cookieDomain)
        .cookie(SsoAuthorizationSession.clearCookie(cookieDomain))
        .build();
  }
}
