package com.meemaw.auth.sso.setup.model;

import lombok.Value;

@Value
public class CreateSsoSetupDTO {

  String organizationId;
  String domain;
  String type;
  String configurationEndpoint;
}
