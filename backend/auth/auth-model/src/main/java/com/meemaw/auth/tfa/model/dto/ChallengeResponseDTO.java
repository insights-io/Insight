package com.meemaw.auth.tfa.model.dto;

import com.meemaw.auth.tfa.MfaMethod;
import java.util.List;
import lombok.Value;

@Value
public class ChallengeResponseDTO {

  String challengeId;
  List<MfaMethod> methods;
}
