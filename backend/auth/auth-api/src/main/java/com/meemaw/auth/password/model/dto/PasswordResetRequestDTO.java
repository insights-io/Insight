package com.meemaw.auth.password.model.dto;

import com.meemaw.shared.validation.Password;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PasswordResetRequestDTO {

  @Password String password;
}
