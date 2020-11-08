package com.meemaw.auth.sso.saml.model.response.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class StatusCode {

  @JacksonXmlProperty(isAttribute = true, localName = "Value")
  String value;
}
