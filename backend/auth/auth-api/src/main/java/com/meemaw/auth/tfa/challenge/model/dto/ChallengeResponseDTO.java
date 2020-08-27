package com.meemaw.auth.tfa.challenge.model.dto;

import com.meemaw.auth.tfa.TfaMethod;
import java.util.List;
import lombok.Value;

@Value
public class ChallengeResponseDTO {

  String challengeId;
  List<TfaMethod> methods;
}
