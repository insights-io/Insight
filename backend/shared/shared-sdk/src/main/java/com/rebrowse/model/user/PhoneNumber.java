package com.rebrowse.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PhoneNumber {

  String countryCode;
  String digits;

  @JsonIgnore
  public String getNumber() {
    return countryCode + digits;
  }
}
