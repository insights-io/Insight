package com.meemaw.auth.tfa.model.dto;

import com.meemaw.auth.tfa.MfaMethod;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MfaSetupDTO {

  OffsetDateTime createdAt;
  MfaMethod method;
}
