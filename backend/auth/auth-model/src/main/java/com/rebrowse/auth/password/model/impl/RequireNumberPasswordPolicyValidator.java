package com.rebrowse.auth.password.model.impl;

import com.rebrowse.auth.password.model.PasswordPolicyValidator;
import com.rebrowse.auth.password.model.PasswordValidationException;

public class RequireNumberPasswordPolicyValidator implements PasswordPolicyValidator {

  public static final PasswordPolicyValidator INSTANCE = new RequireNumberPasswordPolicyValidator();

  @Override
  public void validate(String password) throws PasswordValidationException {
    if (password.chars().anyMatch(Character::isDigit)) {
      return;
    }

    throw new PasswordValidationException("Password should contain at least one number");
  }
}
