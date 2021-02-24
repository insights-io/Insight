package com.rebrowse.auth.accounts.model;

import java.net.URI;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ChooseAccountSsoRedirectResponseDTO extends LocationResponseDTO<ChooseAccountAction> {

  public ChooseAccountSsoRedirectResponseDTO(URI location) {
    super(location, ChooseAccountAction.SSO_REDIRECT);
  }
}
