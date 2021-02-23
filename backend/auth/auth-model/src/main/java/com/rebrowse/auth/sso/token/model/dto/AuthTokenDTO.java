package com.rebrowse.auth.sso.token.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class AuthTokenDTO {

  String token;
  UUID userId;
  OffsetDateTime createdAt;
}
