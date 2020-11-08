package com.meemaw.auth.password.model.impl;

import lombok.Value;

import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordValidationException;

@Value
public class MinCharactersPasswordPolicyValidator implements PasswordPolicyValidator {

  int minCharacters;

  @Override
  public void validate(String password) throws PasswordValidationException {
    if (password.length() < minCharacters) {
      throw new PasswordValidationException(
          String.format("Password should contain at least %d characters", minCharacters));
    }
  }
}
