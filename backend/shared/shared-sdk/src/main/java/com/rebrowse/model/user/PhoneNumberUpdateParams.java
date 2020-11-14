package com.rebrowse.model.user;

import com.rebrowse.model.ApiRequestParams;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PhoneNumberUpdateParams implements ApiRequestParams {

  String countryCode;
  String digits;
}
