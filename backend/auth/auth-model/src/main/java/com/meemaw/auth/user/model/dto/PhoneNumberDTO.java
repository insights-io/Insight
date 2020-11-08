package com.meemaw.auth.user.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.user.model.PhoneNumber;

import javax.validation.constraints.NotBlank;

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
