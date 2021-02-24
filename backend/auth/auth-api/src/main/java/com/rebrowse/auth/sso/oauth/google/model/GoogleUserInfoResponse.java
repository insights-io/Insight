package com.rebrowse.auth.sso.oauth.google.model;

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
public class GoogleUserInfoResponse implements OAuthUserInfo {

  String email;
  String locale;
  String picture;
  String name;
  String sub;

  @JsonProperty("email_verified")
  Boolean emailVerified;

  @JsonProperty("given_name")
  String givenName;

  @JsonProperty("family_name")
  String familyName;

  @Override
  public String getFullName() {
    return String.join(" ", getGivenName(), getFamilyName());
  }
}
