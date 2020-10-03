package com.meemaw.auth.sso.tfa.sms.model.dto;

import lombok.Value;

@Value
public class TfaSmsSetupStartDTO {

  int validitySeconds;
}
