package com.rebrowse.auth.sso.setup.model;

import com.rebrowse.auth.sso.setup.model.dto.SamlConfigurationDTO;
import lombok.Value;

@Value
public class CreateSsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfigurationDTO samlConfiguration;

  public static CreateSsoSetup saml(
      String organizationId, String domain, SamlConfigurationDTO configuration) {
    return new CreateSsoSetup(organizationId, domain, SsoMethod.SAML, configuration);
  }

  public static CreateSsoSetup google(String organizationId, String domain) {
    return new CreateSsoSetup(organizationId, domain, SsoMethod.GOOGLE, null);
  }

  public static CreateSsoSetup github(String organizationId, String domain) {
    return new CreateSsoSetup(organizationId, domain, SsoMethod.GITHUB, null);
  }

  public static CreateSsoSetup microsoft(String organizationId, String domain) {
    return new CreateSsoSetup(organizationId, domain, SsoMethod.MICROSOFT, null);
  }
}
