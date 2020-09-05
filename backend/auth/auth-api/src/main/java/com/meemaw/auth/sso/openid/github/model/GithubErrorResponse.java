package com.meemaw.auth.sso.openid.github.model;

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
public class GithubErrorResponse implements OAuthError {

  @JsonProperty("message")
  String message;

  @JsonProperty("documentation_url")
  String documentationUrl;
}
