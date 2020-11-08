package com.meemaw.auth.sso.setup.model.dto;

import com.meemaw.auth.sso.setup.model.SsoMethod;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class SsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfiguration saml;
  OffsetDateTime createdAt;
}
