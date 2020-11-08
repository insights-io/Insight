package com.meemaw.auth.sso.saml.model.metadata.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Attribute {

  @JacksonXmlProperty(isAttribute = true, localName = "Name")
  String name;

  @JacksonXmlProperty(isAttribute = true, localName = "NameFormat")
  String nameFormat;

  @JacksonXmlProperty(isAttribute = true, localName = "FriendlyName")
  String friendlyName;
}
