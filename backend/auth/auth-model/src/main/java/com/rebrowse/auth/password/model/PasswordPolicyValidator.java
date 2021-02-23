package com.rebrowse.auth.password.model;

import com.rebrowse.auth.password.model.impl.MinCharactersPasswordPolicyValidator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

public interface PasswordPolicyValidator {

  List<PasswordPolicyValidator> DEFAULT_VALIDATORS =
      List.of(new MinCharactersPasswordPolicyValidator(8));

  static void validateFirstPassword(@Nullable PasswordPolicy policy, String newPassword)
      throws PasswordValidationException {
    validate(policy, null, newPassword);
  }

  static void validate(
      @Nullable PasswordPolicy policy, @Nullable String currentPassword, String newPassword)
      throws PasswordValidationException {
    List<PasswordPolicyValidator> validators =
        Optional.ofNullable(policy)
            .map(p -> p.getValidators(currentPassword))
            .orElse(DEFAULT_VALIDATORS);

    for (PasswordPolicyValidator validator : validators) {
      validator.validate(newPassword);
    }
  }

  void validate(String newPassword) throws PasswordValidationException;
}
