package com.meemaw.auth.sso.saml.model.metadata.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SingleLogoutService {

  @JacksonXmlProperty(isAttribute = true)
  String Binding;

  @JacksonXmlProperty(isAttribute = true)
  String Location;
}
