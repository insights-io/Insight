package com.meemaw.auth.sso.setup.model;

import java.net.URL;
import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SsoSetupDTO {

  String organizationId;
  String domain;
  SsoMethod method;
  URL configurationEndpoint;
  OffsetDateTime createdAt;
}
