package com.meemaw.auth.sso.session.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SsoSocialLogin {

  LoginResult<?> loginResult;
  String Location;
  String cookieDomain;
}
