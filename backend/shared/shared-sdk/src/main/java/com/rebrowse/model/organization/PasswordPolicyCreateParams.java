package com.rebrowse.model.organization;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;

@Value
@Builder
public class PasswordPolicyCreateParams implements ApiRequestParams {

  short minCharacters;
  boolean preventPasswordReuse;
  boolean requireUppercaseCharacter;
  boolean requireLowercaseCharacter;
  boolean requireNumber;
  boolean requireNonAlphanumericCharacter;
}
