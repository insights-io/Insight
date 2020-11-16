package com.meemaw.auth.mfa.model.dto;

import com.meemaw.auth.mfa.MfaMethod;
import java.util.List;
import lombok.Value;

@Value
public class ChallengeResponseDTO {

  String challengeId;
  List<MfaMethod> methods;
}
