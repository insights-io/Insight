package com.meemaw.auth.sso.setup.model.dto;

import com.meemaw.auth.sso.setup.model.SamlMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.net.URL;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SamlConfiguration {

  @NotNull(message = "Required")
  SamlMethod method;

  @NotNull(message = "Required")
  URL metadataEndpoint;

  public static SamlConfiguration okta(URL metadataEndpoint) {
    return new SamlConfiguration(SamlMethod.OKTA, metadataEndpoint);
  }

  public static SamlConfiguration auth0(URL metadataEndpoint) {
    return new SamlConfiguration(SamlMethod.AUTH0, metadataEndpoint);
  }

  public static SamlConfiguration onelogin(URL metadataEndpoint) {
    return new SamlConfiguration(SamlMethod.ONELOGIN, metadataEndpoint);
  }
}
