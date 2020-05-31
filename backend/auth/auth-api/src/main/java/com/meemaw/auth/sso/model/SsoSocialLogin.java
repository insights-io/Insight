package com.meemaw.auth.sso.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SsoSocialLogin {

  String sessionId;
  String Location;
  String cookieDomain;
}
