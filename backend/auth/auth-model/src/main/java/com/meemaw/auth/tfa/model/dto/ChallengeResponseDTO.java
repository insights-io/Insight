package com.meemaw.auth.tfa.model.dto;

import lombok.Value;

import com.meemaw.auth.tfa.TfaMethod;

import java.util.List;

@Value
public class ChallengeResponseDTO {

  String challengeId;
  List<TfaMethod> methods;
}
