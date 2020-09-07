package com.meemaw.auth.sso.setup.model;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class SsoSetupDTO {

  String organizationId;
  String domain;
  String type;
  String configurationEndpoint;
  OffsetDateTime createdAt;
}
