package com.rebrowse.auth.password.model.impl;

import com.rebrowse.auth.password.model.PasswordPolicyValidator;
import com.rebrowse.auth.password.model.PasswordValidationException;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import org.mindrot.jbcrypt.BCrypt;

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
