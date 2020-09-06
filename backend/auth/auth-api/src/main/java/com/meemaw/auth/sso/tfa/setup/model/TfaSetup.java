package com.meemaw.auth.sso.tfa.setup.model;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.setup.dto.TfaSetupDTO;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface TfaSetup {

  UUID getUserId();

  OffsetDateTime getCreatedAt();

  TfaMethod getMethod();

  default TfaSetupDTO dto() {
    return new TfaSetupDTO(getCreatedAt(), getMethod());
  }
}
