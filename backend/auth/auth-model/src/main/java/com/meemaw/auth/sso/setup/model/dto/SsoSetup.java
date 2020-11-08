package com.meemaw.auth.sso.setup.model.dto;

import com.meemaw.auth.sso.setup.model.SsoMethod;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfiguration saml;
  OffsetDateTime createdAt;
}
