package com.meemaw.auth.sso.setup.model.dto;

import com.meemaw.auth.sso.setup.model.SsoMethod;
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

  URL configurationEndpoint;

  public static CreateSsoSetupDTO google() {
    return new CreateSsoSetupDTO(SsoMethod.GOOGLE, null);
  }
}
