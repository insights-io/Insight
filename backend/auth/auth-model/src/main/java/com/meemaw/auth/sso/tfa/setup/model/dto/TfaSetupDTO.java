package com.meemaw.auth.sso.tfa.setup.model.dto;

import com.meemaw.auth.sso.tfa.TfaMethod;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TfaSetupDTO {

  OffsetDateTime createdAt;
  TfaMethod method;
}
