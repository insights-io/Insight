package com.meemaw.auth.sso.setup.model;

import lombok.Value;

import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;

@Value
public class CreateSsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  SamlConfiguration samlConfiguration;
}
