package com.rebrowse.auth.mfa.model;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.dto.MfaSetupDTO;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface MfaConfiguration {

  UUID getUserId();

  OffsetDateTime getCreatedAt();

  MfaMethod getMethod();

  default MfaSetupDTO dto() {
    return new MfaSetupDTO(getCreatedAt(), getMethod());
  }
}
