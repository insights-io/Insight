package com.rebrowse.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.model.ApiRequestParams;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PhoneNumber implements ApiRequestParams {

  String countryCode;
  String digits;

  @JsonIgnore
  public String getNumber() {
    return countryCode + digits;
  }
}
