package com.meemaw.auth.tfa.dto;

import lombok.Value;

@Value
public class MfaChallengeCodeDetailsDTO {

  int validitySeconds;
}
