package com.meemaw.auth.sso.oauth.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.sso.oauth.OAuthUserInfo;

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
