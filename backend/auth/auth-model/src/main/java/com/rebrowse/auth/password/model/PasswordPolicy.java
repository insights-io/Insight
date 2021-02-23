package com.rebrowse.auth.password.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.auth.organization.model.dto.PasswordPolicyDTO;
import com.rebrowse.auth.password.model.impl.MinCharactersPasswordPolicyValidator;
import com.rebrowse.auth.password.model.impl.PreventPasswordReusePasswordPolicyValidator;
import com.rebrowse.auth.password.model.impl.RequireLowercaseCharacterPasswordPolicyValidator;
import com.rebrowse.auth.password.model.impl.RequireNonAlphanumericCharacterPasswordPolicyValidator;
import com.rebrowse.auth.password.model.impl.RequireNumberPasswordPolicyValidator;
import com.rebrowse.auth.password.model.impl.RequireUppercaseCharacterPasswordPolicyValidator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public interface PasswordPolicy {

  short getMinCharacters();

  boolean isPreventPasswordReuse();

  boolean isRequireUppercaseCharacter();

  boolean isRequireLowercaseCharacter();

  boolean isRequireNumber();

  boolean isRequireNonAlphanumericCharacter();

  OffsetDateTime getUpdatedAt();

  OffsetDateTime getCreatedAt();

  @JsonIgnore
  default PasswordPolicyDTO dto(String organizationId) {
    return new PasswordPolicyDTO(
        organizationId,
        getMinCharacters(),
        isPreventPasswordReuse(),
        isRequireUppercaseCharacter(),
        isRequireLowercaseCharacter(),
        isRequireNumber(),
        isRequireNonAlphanumericCharacter(),
        getUpdatedAt(),
        getCreatedAt());
  }

  @JsonIgnore
  default List<PasswordPolicyValidator> getValidators(@Nullable String currentPasswordHashed) {
    List<PasswordPolicyValidator> validators = new ArrayList<>();
    validators.add(new MinCharactersPasswordPolicyValidator(getMinCharacters()));
    if (isPreventPasswordReuse()) {
      validators.add(new PreventPasswordReusePasswordPolicyValidator(currentPasswordHashed));
    }
    if (isRequireUppercaseCharacter()) {
      validators.add(RequireUppercaseCharacterPasswordPolicyValidator.INSTANCE);
    }
    if (isRequireLowercaseCharacter()) {
      validators.add(RequireLowercaseCharacterPasswordPolicyValidator.INSTANCE);
    }
    if (isRequireNumber()) {
      validators.add(RequireNumberPasswordPolicyValidator.INSTANCE);
    }
    if (isRequireNonAlphanumericCharacter()) {
      validators.add(RequireNonAlphanumericCharacterPasswordPolicyValidator.INSTANCE);
    }
    return validators;
  }
}
