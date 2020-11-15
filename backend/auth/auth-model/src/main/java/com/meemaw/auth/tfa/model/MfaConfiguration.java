package com.meemaw.auth.tfa.model;

import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.model.dto.MfaSetupDTO;
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
