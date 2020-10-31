package com.meemaw.auth.password.model.impl;

import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordValidationException;

public class RequireNonAlphanumericCharacterPasswordPolicyValidator
    implements PasswordPolicyValidator {

  public static final PasswordPolicyValidator INSTANCE =
      new RequireNonAlphanumericCharacterPasswordPolicyValidator();

  @Override
  public void validate(String password) throws PasswordValidationException {
    if (password
        .chars()
        .anyMatch(
            character ->
                !Character.isDigit(character)
                    && !Character.isLetter(character)
                    && !Character.isWhitespace(character))) {
      return;
    }

    throw new PasswordValidationException(
        "Password should contain at least one non-alphanumeric character");
  }
}
