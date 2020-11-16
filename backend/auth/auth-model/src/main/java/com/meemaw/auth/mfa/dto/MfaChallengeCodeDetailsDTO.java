package com.meemaw.auth.mfa.dto;

import lombok.Value;

@Value
public class MfaChallengeCodeDetailsDTO {

  int validitySeconds;
}
