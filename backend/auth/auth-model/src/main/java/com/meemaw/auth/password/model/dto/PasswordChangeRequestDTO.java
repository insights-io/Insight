package com.meemaw.auth.password.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.shared.rest.response.Boom;

import java.util.Map;
import javax.validation.constraints.NotBlank;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PasswordChangeRequestDTO {

  @NotBlank(message = "Required")
  String currentPassword;

  @NotBlank(message = "Required")
  String newPassword;

  @NotBlank(message = "Required")
  String confirmNewPassword;

  public void validate() {
    if (currentPassword.equals(newPassword)) {
      throw Boom.badRequest()
          .errors(Map.of("newPassword", "New password cannot be the same as the previous one!"))
          .exception();
    }
    if (!confirmNewPassword.equals(newPassword)) {
      throw Boom.badRequest().message("Passwords must match!").exception();
    }
  }
}
