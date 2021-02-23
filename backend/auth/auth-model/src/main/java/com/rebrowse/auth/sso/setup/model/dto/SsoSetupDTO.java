package com.rebrowse.auth.sso.setup.model.dto;

import com.rebrowse.auth.sso.setup.model.SsoMethod;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SsoSetupDTO {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfigurationDTO saml;
  OffsetDateTime createdAt;
}
