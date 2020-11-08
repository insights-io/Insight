package com.meemaw.auth.sso.saml.model.metadata.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class IDPSSODescriptor {

  @JacksonXmlProperty(isAttribute = true)
  String protocolSupportEnumeration;

  @JacksonXmlProperty(isAttribute = true, localName = "WantAuthnRequestsSigned")
  boolean wantAuthnRequestsSigned;

  @JacksonXmlProperty(localName = "KeyDescriptor")
  KeyDescriptor keyDescriptor;

  @JacksonXmlProperty(localName = "SingleLogoutService")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<SingleLogoutService> singleLogoutServices;

  @JacksonXmlProperty(localName = "SingleSignOnService")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<SingleSignOnService> singleSignOnServices;

  @JacksonXmlProperty(localName = "Attribute")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<Attribute> attributes;

  @JacksonXmlProperty(localName = "NameIDFormat")
  @JacksonXmlElementWrapper(useWrapping = false)
  List<String> NameIDFormat;
}
