package com.rebrowse.auth.sso.saml.model;

import lombok.Value;
import org.opensaml.xmlsec.signature.Signature;

@Value
public class SamlCoreDataResponse {

  String email;
  String fullName;
  String issuer;
  Signature signature;
}
