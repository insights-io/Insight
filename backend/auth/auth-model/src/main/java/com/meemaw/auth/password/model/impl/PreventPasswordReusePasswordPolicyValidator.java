package com.meemaw.auth.password.model.impl;

import org.mindrot.jbcrypt.BCrypt;

import com.meemaw.auth.password.model.PasswordPolicyValidator;
import com.meemaw.auth.password.model.PasswordValidationException;

import java.util.function.BiFunction;
import javax.annotation.Nullable;

public class PreventPasswordReusePasswordPolicyValidator implements PasswordPolicyValidator {

  private final String currentPasswordHashed;
  private final BiFunction<String, String, Boolean> passwordMatcher;

  public PreventPasswordReusePasswordPolicyValidator(@Nullable String currentPasswordHashed) {
    this.currentPasswordHashed = currentPasswordHashed;
    this.passwordMatcher = BCrypt::checkpw;
  }

  @Override
  public void validate(String newPassword) throws PasswordValidationException {
    if (currentPasswordHashed == null) {
      return;
    }

    if (passwordMatcher.apply(newPassword, currentPasswordHashed)) {
      throw new PasswordValidationException("New password cannot be the same as the previous one!");
    }
  }
}
