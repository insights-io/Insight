package com.meemaw.auth.sso.openid.google.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meemaw.auth.sso.model.OAuthError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleErrorResponse implements OAuthError {

  @JsonProperty("error")
  String error;

  @JsonProperty("error_description")
  String description;

  @Override
  public String getMessage() {
    return String.format("%s. %s", error, description);
  }
}
