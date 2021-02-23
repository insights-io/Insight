package com.rebrowse.auth.sso.oauth.google.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenResponse {

  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("expires_in")
  String expiresIn;

  String scope;

  @JsonProperty("token_type")
  String tokenType;

  @JsonProperty("id_token")
  String idToken;
}
