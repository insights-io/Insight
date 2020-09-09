package com.meemaw.auth.sso.setup.model;

import java.net.URL;
import lombok.Value;

@Value
public class CreateSsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  URL configurationEndpoint;
}
