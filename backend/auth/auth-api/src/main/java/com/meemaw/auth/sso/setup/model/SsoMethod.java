package com.meemaw.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import java.util.Optional;
import javax.annotation.Nullable;
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

  public String signInLocation(String serverBaseUrl, String email, @Nullable String redirect) {
    UriBuilder builder = UriBuilder.fromUri(serverBaseUrl);
    if (!SsoMethod.SAML.equals(this)) {
      builder = builder.path(OAuth2Resource.PATH);
    } else {
      builder = builder.path(SsoResource.PATH);
    }

    builder
        .path(key)
        .path(SamlResource.SIGNIN_PATH)
        .queryParam("redirect", Optional.ofNullable(redirect).orElse("/"))
        .queryParam("email", email);

    return builder.build().toString();
  }
}
