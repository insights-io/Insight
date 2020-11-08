package com.meemaw.auth.sso.token.model;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CreateAuthTokenParams {

  String token;
  UUID userId;
}
