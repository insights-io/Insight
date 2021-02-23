package com.rebrowse.auth.password.model.impl;

import com.rebrowse.auth.password.model.PasswordPolicyValidator;
import com.rebrowse.auth.password.model.PasswordValidationException;

public class RequireLowercaseCharacterPasswordPolicyValidator implements PasswordPolicyValidator {

  public static final PasswordPolicyValidator INSTANCE =
      new RequireLowercaseCharacterPasswordPolicyValidator();

  @Override
  public void validate(String password) throws PasswordValidationException {
    if (password.chars().anyMatch(Character::isLowerCase)) {
      return;
    }

    throw new PasswordValidationException(
        "Password should contain at least one lowercase character");
  }
}
