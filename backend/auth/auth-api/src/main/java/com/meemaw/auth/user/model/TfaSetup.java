package com.meemaw.auth.user.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class TfaSetup {

  String secret;
  OffsetDateTime createdAt;
}
