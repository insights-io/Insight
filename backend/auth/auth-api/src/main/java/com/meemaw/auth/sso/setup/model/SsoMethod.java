package com.meemaw.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;

public enum SsoMethod {
  SAML(LoginMethod.SAML.getKey()),
  GOOGLE(LoginMethod.GOOGLE.getKey()),
  MICROSOFT(LoginMethod.MICROSOFT.getKey()),
  GITHUB(LoginMethod.GITHUB.getKey());

  private final String key;

  SsoMethod(String key) {
    this.key = key;
  }

  @JsonCreator
  public static SsoMethod fromString(String key) {
    return SsoMethod.valueOf(key == null ? null : key.toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return key;
  }

  public String signInLocation(String email, URL redirect, URI serverBaseURI) {
    UriBuilder builder = UriBuilder.fromUri(serverBaseURI);
    if (!SsoMethod.SAML.equals(this)) {
      builder = builder.path(OAuth2Resource.PATH);
    } else {
      builder = builder.path(SsoResource.PATH);
    }

    builder
        .path(key)
        .path(OAuth2Resource.SIGNIN_PATH)
        .queryParam("redirect", redirect)
        .queryParam("email", email);

    return builder.build().toString();
  }
}
