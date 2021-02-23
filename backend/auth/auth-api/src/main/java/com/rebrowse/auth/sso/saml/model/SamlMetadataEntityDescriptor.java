package com.rebrowse.auth.sso.saml.model;

import java.util.List;
import java.util.Optional;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;

public class SamlMetadataEntityDescriptor {

  private static final String PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
  private static final String HTTP_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
  private static final String HTTP_REDIRECT_BINDING =
      "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";

  private final EntityDescriptor entityDescriptor;

  public SamlMetadataEntityDescriptor(EntityDescriptor entityDescriptor) {
    this.entityDescriptor = entityDescriptor;
  }

  public String getEntityId() {
    return entityDescriptor.getEntityID();
  }

  private IDPSSODescriptor getIdpSsoDescriptor() {
    return entityDescriptor.getIDPSSODescriptor(PROTOCOL);
  }

  public String getCertificate() {
    return getIdpSsoDescriptor()
        .getKeyDescriptors()
        .get(0)
        .getKeyInfo()
        .getX509Datas()
        .get(0)
        .getX509Certificates()
        .get(0)
        .getValue();
  }

  public Optional<SingleSignOnService> getHttpPostSignOnService() {
    return findSingleSignOnService(HTTP_POST_BINDING);
  }

  public Optional<SingleSignOnService> getHttpRedirectSignOnService() {
    return findSingleSignOnService(HTTP_REDIRECT_BINDING);
  }

  private Optional<SingleSignOnService> findSingleSignOnService(String binding) {
    return getIdpSsoDescriptor().getSingleSignOnServices().stream()
        .filter(sso -> sso.getBinding().equals(binding))
        .findFirst();
  }

  public Optional<SingleLogoutService> getHttpPostLogoutService() {
    return findLogoutService(HTTP_POST_BINDING);
  }

  public Optional<SingleLogoutService> getHttpRedirectLogoutService() {
    return findLogoutService(HTTP_REDIRECT_BINDING);
  }

  private Optional<SingleLogoutService> findLogoutService(String binding) {
    return getIdpSsoDescriptor().getSingleLogoutServices().stream()
        .filter(service -> service.getBinding().equals(binding))
        .findFirst();
  }

  public List<Attribute> getAttributes() {
    return getIdpSsoDescriptor().getAttributes();
  }
}
