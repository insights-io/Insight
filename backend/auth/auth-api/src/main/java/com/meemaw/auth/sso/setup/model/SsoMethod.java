package com.meemaw.auth.sso.setup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

public enum SsoMethod {
  SAML("saml");

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

  public String getSsoServerRedirect(
      String serverBaseUrl, String email, @Nullable String redirect) {
    UriBuilder builder =
        UriBuilder.fromUri(serverBaseUrl)
            .path(SsoResource.PATH)
            .queryParam("email", email)
            .queryParam("redirect", Optional.ofNullable(redirect).orElse("/"))
            .path(key)
            .path(SamlResource.SIGNIN_PATH);

    return builder.build().toString();
  }
}
