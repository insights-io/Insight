package com.meemaw.auth.password.model.dto;

import com.meemaw.shared.validation.Password;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PasswordResetRequestDTO {

  @NotBlank(message = "Required")
  @Email
  String email;

  @NotBlank(message = "Required")
  String org;

  @NotNull(message = "Required")
  UUID token;

  @Password
  String password;

}
