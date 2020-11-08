package com.meemaw.auth.tfa.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.tfa.TfaMethod;

import java.time.OffsetDateTime;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TfaSetupDTO {

  OffsetDateTime createdAt;
  TfaMethod method;
}
