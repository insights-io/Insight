package com.meemaw.auth.password.model.impl;

import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordValidationException;

public class RequireNumberPasswordPolicyValidator implements PasswordPolicyValidator {

  public static PasswordPolicyValidator INSTANCE = new RequireNumberPasswordPolicyValidator();

  @Override
  public void validate(String password) throws PasswordValidationException {
    if (password.chars().anyMatch(Character::isDigit)) {
      return;
    }

    throw new PasswordValidationException("Password should contain at least one number");
  }
}
