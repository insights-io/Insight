package com.meemaw.auth.sso.saml.model;

import lombok.Value;
import org.opensaml.xmlsec.signature.Signature;

@Value
public class SamlDataResponse {

  String email;
  String fullName;
  String issuer;
  Signature signature;
}
