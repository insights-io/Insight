package com.rebrowse.auth.sso.oauth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebrowse.auth.sso.oauth.OAuthError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicrosoftErrorResponse implements OAuthError {

  String error;

  @JsonProperty("error_description")
  String description;

  @Override
  public String getMessage() {
    if (description == null) {
      return error;
    }
    return description.split("\r")[0];
  }
}
