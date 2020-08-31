package com.meemaw.auth.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PhoneNumberDTO implements PhoneNumber {

  @NotBlank(message = "Required")
  String countryCode;

  @NotBlank(message = "Required")
  String digits;

  @JsonIgnore
  public String getNumber() {
    return countryCode + digits;
  }
}
