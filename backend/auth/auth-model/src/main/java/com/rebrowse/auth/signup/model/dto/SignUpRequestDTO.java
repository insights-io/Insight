package com.rebrowse.auth.signup.model.dto;

import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.shared.validation.Password;
import java.net.URL;
import javax.validation.Valid;
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
public class SignUpRequestDTO {

  @NotNull(message = "Required")
  URL redirect;

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
