package com.rebrowse.auth.password.model.dto;

import java.net.URL;
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
public class PasswordForgotRequestDTO {

  @NotBlank(message = "Required")
  @Email
  String email;

  @NotNull(message = "Required")
  URL redirect;
}
