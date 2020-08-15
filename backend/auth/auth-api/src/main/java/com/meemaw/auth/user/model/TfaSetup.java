package com.meemaw.auth.user.model;

import com.meemaw.auth.user.model.dto.TfaSetupDTO;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class TfaSetup {

  String secret;
  OffsetDateTime createdAt;

  public TfaSetupDTO dto() {
    return new TfaSetupDTO(createdAt);
  }
}
