package com.meemaw.auth.tfa.dto;

import lombok.Value;

@Value
public class TfaChallengeCodeDetailsDTO {

  int validitySeconds;
}
