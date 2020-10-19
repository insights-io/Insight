package com.meemaw.auth.signup.model.dto;

import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.shared.validation.Password;
import javax.validation.Valid;
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

  /**
   * We use the @Password validator here as this is the sign up flow that created a new organization
   * and password policy cannot be applied yet.
   */
  @Password String password;

  @NotBlank(message = "Required")
  String fullName;

  @NotBlank(message = "Required")
  String company;

  @Valid PhoneNumberDTO phoneNumber;
}
