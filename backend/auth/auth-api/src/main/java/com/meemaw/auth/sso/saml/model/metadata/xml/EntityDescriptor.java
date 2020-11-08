package com.meemaw.auth.sso.saml.model.metadata.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class EntityDescriptor {

  private static final String HTTP_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
  private static final String HTTP_REDIRECT_BINDING =
      "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";

  @JacksonXmlProperty(isAttribute = true, localName = "entityID")
  String entityId;

  @JacksonXmlProperty(localName = "IDPSSODescriptor")
  IDPSSODescriptor idpSsoDescriptor;

  public String getCertificate() {
    return idpSsoDescriptor
        .getKeyDescriptor()
        .getKeyInfo()
        .getX509Data()
        .getX509Certificate()
        .replaceAll("[\\n\\t ]", "");
  }

  public Optional<SingleLogoutService> getHttpPostLogoutService() {
    return findLogoutService(HTTP_POST_BINDING);
  }

  public Optional<SingleLogoutService> getHttpRedirectLogoutService() {
    return findLogoutService(HTTP_REDIRECT_BINDING);
  }

  public Optional<SingleSignOnService> getHttpPostSignOnService() {
    return findSignOnService(HTTP_POST_BINDING);
  }

  public Optional<SingleSignOnService> getHttpRedirectSignOnService() {
    return findSignOnService(HTTP_REDIRECT_BINDING);
  }

  private Optional<SingleSignOnService> findSignOnService(String binding) {
    return idpSsoDescriptor.getSingleSignOnServices().stream()
        .filter(service -> service.getBinding().equals(binding))
        .findFirst();
  }

  private Optional<SingleLogoutService> findLogoutService(String binding) {
    return idpSsoDescriptor.getSingleLogoutServices().stream()
        .filter(service -> service.getBinding().equals(binding))
        .findFirst();
  }
}
