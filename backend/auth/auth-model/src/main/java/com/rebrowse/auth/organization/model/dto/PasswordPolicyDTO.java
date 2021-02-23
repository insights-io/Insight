package com.rebrowse.auth.organization.model.dto;

import com.rebrowse.auth.password.model.PasswordPolicy;
import java.time.OffsetDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PasswordPolicyDTO implements PasswordPolicy {

  String organizationId;

  @Min(value = 8, message = "Password should be at least 8 characters long")
  @Max(value = 256, message = "Password should be at most 256 characters long")
  short minCharacters;

  boolean preventPasswordReuse;
  boolean requireUppercaseCharacter;
  boolean requireLowercaseCharacter;
  boolean requireNumber;
  boolean requireNonAlphanumericCharacter;
  OffsetDateTime updatedAt;
  OffsetDateTime createdAt;
}
