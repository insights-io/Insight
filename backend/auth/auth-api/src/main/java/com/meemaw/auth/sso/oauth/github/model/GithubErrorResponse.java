package com.meemaw.auth.sso.oauth.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.sso.oauth.OAuthError;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubErrorResponse implements OAuthError {

  @JsonProperty("message")
  String message;

  @JsonProperty("documentation_url")
  String documentationUrl;
}
