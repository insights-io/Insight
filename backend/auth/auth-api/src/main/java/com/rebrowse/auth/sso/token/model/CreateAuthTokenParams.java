package com.rebrowse.auth.sso.token.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateAuthTokenParams {

  String token;
  UUID userId;
}
