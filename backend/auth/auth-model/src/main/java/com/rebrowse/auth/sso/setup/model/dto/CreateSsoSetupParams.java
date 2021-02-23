package com.rebrowse.auth.sso.setup.model.dto;

import com.rebrowse.auth.sso.setup.model.SsoMethod;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class CreateSsoSetupParams {

  @NotNull(message = "Required")
  SsoMethod method;

  SamlConfigurationDTO saml;

  public static CreateSsoSetupParams google() {
    return new CreateSsoSetupParams(SsoMethod.GOOGLE, null);
  }
}
