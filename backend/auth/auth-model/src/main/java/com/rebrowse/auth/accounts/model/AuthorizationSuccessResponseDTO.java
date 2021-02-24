package com.rebrowse.auth.accounts.model;

import java.net.URI;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class AuthorizationSuccessResponseDTO extends LocationResponseDTO<AuthorizationAction> {

  public AuthorizationSuccessResponseDTO(URI location) {
    super(location, AuthorizationAction.SUCCESS);
  }
}
