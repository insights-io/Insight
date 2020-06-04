package com.meemaw.auth.signup.model.dto;

import com.meemaw.shared.validation.Password;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SignUpRequestDTO {

  @NotBlank(message = "Required")
  @Email
  String email;

  @Password String password;

  @NotBlank(message = "Required")
  String fullName;

  @NotBlank(message = "Required")
  String company;

  String phoneNumber;
}
