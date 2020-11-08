package com.rebrowse.model.organization;

import com.rebrowse.model.ApiRequestParams;
import lombok.Builder;
import lombok.Value;

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
