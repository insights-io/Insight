package com.rebrowse.auth.sso.setup.model.dto;

import com.rebrowse.auth.sso.setup.model.SamlMethod;
import java.net.URL;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SamlConfigurationDTO {

  @NotNull(message = "Required")
  SamlMethod method;

  @NotNull(message = "Required")
  URL metadataEndpoint;

  public static SamlConfigurationDTO okta(URL metadataEndpoint) {
    return new SamlConfigurationDTO(SamlMethod.OKTA, metadataEndpoint);
  }

  public static SamlConfigurationDTO auth0(URL metadataEndpoint) {
    return new SamlConfigurationDTO(SamlMethod.AUTH0, metadataEndpoint);
  }

  public static SamlConfigurationDTO onelogin(URL metadataEndpoint) {
    return new SamlConfigurationDTO(SamlMethod.ONELOGIN, metadataEndpoint);
  }
}
