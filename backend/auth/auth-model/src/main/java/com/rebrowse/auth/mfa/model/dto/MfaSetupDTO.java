package com.rebrowse.auth.mfa.model.dto;

import com.rebrowse.auth.mfa.MfaMethod;
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
