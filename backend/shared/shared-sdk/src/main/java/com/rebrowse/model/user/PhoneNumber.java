package com.rebrowse.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;

@Value
public class PhoneNumber {

  String countryCode;
  String digits;

  @JsonIgnore
  public String getNumber() {
    return countryCode + digits;
  }
}
