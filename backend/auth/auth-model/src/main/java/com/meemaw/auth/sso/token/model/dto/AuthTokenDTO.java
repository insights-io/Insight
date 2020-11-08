package com.meemaw.auth.sso.token.model.dto;

import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
public class AuthTokenDTO {

  String token;
  UUID userId;
  OffsetDateTime createdAt;
}
