package com.meemaw.auth.sso.setup.model;

import java.net.URL;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class CreateSsoSetupDTO {

  @NotNull(message = "Required")
  SsoMethod method;

  @NotNull(message = "Required")
  URL configurationEndpoint;
}
