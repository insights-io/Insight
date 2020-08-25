package com.meemaw.auth.tfa.sms.model.dto;

import lombok.Value;

@Value
public class TfaSmsSetupStartDTO {

  int validitySeconds;
}
