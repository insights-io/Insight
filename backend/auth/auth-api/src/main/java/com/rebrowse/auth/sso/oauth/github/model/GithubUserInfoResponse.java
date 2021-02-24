package com.rebrowse.auth.sso.oauth.github.model;

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
public class GithubUserInfoResponse implements OAuthUserInfo {

  @JsonProperty("login")
  String username;

  @JsonProperty("name")
  String fullName;

  String email;

  @JsonProperty("avatar_url")
  String picture;
}
