package com.meemaw.auth.sso.setup.model;

import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import lombok.Value;

@Value
public class CreateSsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfiguration samlConfiguration;
}
