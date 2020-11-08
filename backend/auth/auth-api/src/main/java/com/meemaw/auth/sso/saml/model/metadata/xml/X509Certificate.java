package com.meemaw.auth.sso.saml.model.metadata.xml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class X509Certificate {

  String value;
}
