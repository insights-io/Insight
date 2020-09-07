package com.meemaw.auth.sso.saml.model;

import lombok.Value;

@Value
public class SamlMetadataResponse {

  String certificate;
  String ssoHttpPostBinding;
  String ssoHttpRedirectBinding;
  String entityId;
}
