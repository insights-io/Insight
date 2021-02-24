package com.rebrowse.auth.sso.oauth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebrowse.auth.sso.oauth.OAuthUserInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicrosoftUserInfoResponse implements OAuthUserInfo {

  String sub;
  String email;

  @JsonProperty("name")
  String fullName;

  @JsonProperty("family_name")
  String familyName;

  @JsonProperty("given_name")
  String givenName;
}
