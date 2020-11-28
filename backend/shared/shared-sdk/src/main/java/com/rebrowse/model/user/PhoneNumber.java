package com.rebrowse.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.model.ApiRequestParams;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PhoneNumber implements ApiRequestParams {

  String countryCode;
  String digits;

  @JsonIgnore
  public String getNumber() {
    return countryCode + digits;
  }
}
