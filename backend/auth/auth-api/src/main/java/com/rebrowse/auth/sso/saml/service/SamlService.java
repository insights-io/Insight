package com.rebrowse.auth.sso.saml.service;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.request.SsoAuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.saml.client.SamlClient;
import com.rebrowse.auth.sso.saml.model.SamlCoreDataResponse;
import com.rebrowse.auth.sso.saml.model.SamlMetadataEntityDescriptor;
import com.rebrowse.auth.sso.saml.resource.v1.SamlResource;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.shared.logging.LoggingConstants;
import com.rebrowse.shared.rest.exception.BoomException;
import com.rebrowse.shared.rest.response.Boom;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class SamlService extends AbstractIdentityProvider {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoService ssoService;
  @Inject SamlParser samlParser;
  @Inject SamlClient samlClient;

  @Override
  public SsoMethod getMethod() {
    return SsoMethod.SAML;
  }

  @Override
  public String getResourcePath() {
    return SamlResource.PATH;
  }

  @Override
  public URI buildAuthorizationUri(
      String state, URI serverRedirectUri, @Nullable String loginHint) {
    return null;
  }

  public URI buildAuthorizationUri(String state, SamlMetadataEntityDescriptor entityDescriptor) {
    return UriBuilder.fromUri(entityDescriptor.getHttpPostSignOnService().get().getLocation())
        .queryParam("RelayState", state)
        .build();
  }

  private void validateSignature(
      Signature signature, SamlMetadataEntityDescriptor entityDescriptor) {
    try {
      SignatureValidator.validate(
          signature, samlParser.certificate(entityDescriptor.getCertificate()));
    } catch (SignatureException | CertificateException ex) {
      log.error("[AUTH]: SAML callback signature exception", ex);
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  public void validateSignature(
      SamlCoreDataResponse samlCoreDataResponse, SamlMetadataEntityDescriptor entityDescriptor) {
    validateSignature(samlCoreDataResponse.getSignature(), entityDescriptor);
  }

  private SamlMetadataEntityDescriptor retrieveMetadataCatchy(URL metadataEndpoint) {
    try {
      return retrieveMetadata(metadataEndpoint);
    } catch (FileNotFoundException ex) {
      throw retrieveMetadataException(metadataEndpoint, "Failed to retrieve: Not Found", ex);
    } catch (XMLParserException | UnmarshallingException ex) {
      throw retrieveMetadataException(metadataEndpoint, "Failed to retrieve: Malformed XML", ex);
    } catch (IOException ex) {
      throw retrieveMetadataException(
          metadataEndpoint, String.format("Failed to retrieve: %s", ex.getMessage()), ex);
    }
  }

  private BoomException retrieveMetadataException(
      URL metadataEndpoint, String message, Exception ex) {
    log.error("[AUTH]: Failed to fetch SAML metadata from={}", metadataEndpoint, ex);
    throw Boom.badRequest()
        .errors(Map.of("saml", Map.of("metadataEndpoint", message)))
        .exception(ex);
  }

  private SamlMetadataEntityDescriptor retrieveMetadata(URL metadataEndpoint)
      throws IOException, XMLParserException, UnmarshallingException {
    return samlParser.parseSamlMetadataEntityDescriptor(samlClient.fetchMetadata(metadataEndpoint));
  }

  public CompletionStage<AuthorizationResponse> handleCallback(
      String samlResponse, String relayState, URI serverBaseUri) {
    SamlCoreDataResponse samlCoreDataResponse;
    try {
      samlCoreDataResponse = samlParser.parseSamlCoreResponse(samlResponse);
    } catch (UnmarshallingException | XMLParserException ex) {
      log.error("[AUTH]: Failed to decode SAMLResponse={}", samlResponse, ex);
      throw Boom.badRequest().message("Invalid SAMLResponse").exception(ex);
    }

    if (samlCoreDataResponse.getSignature() == null) {
      log.error("[AUTH]: SAML callback missing signature SAMLResponse={}", samlResponse);
      throw Boom.badRequest().message("Missing signature").exception();
    }

    String fullName = samlCoreDataResponse.getFullName();
    String email = samlCoreDataResponse.getEmail();
    String domain = EmailUtils.getDomain(email);

    return ssoSetupDatasource
        .getByDomain(domain)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                log.info("[AUTH]: SSO not configured for email={}", email);
                throw Boom.badRequest()
                    .message("That email or domain isn’t registered for SSO.")
                    .exception();
              }

              String organizationId = maybeSsoSetup.get().getOrganizationId();
              MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
              URL configurationEndpoint = maybeSsoSetup.get().getSaml().getMetadataEndpoint();
              SamlMetadataEntityDescriptor entityDescriptor =
                  retrieveMetadataCatchy(configurationEndpoint);
              if (!entityDescriptor.getEntityId().equals(samlCoreDataResponse.getIssuer())) {
                log.error(
                    "[AUTH]: SAML callback entity miss-match expected={} actual={} organization={}",
                    entityDescriptor.getEntityId(),
                    samlCoreDataResponse.getIssuer(),
                    organizationId);
                throw Boom.badRequest().message("Invalid entityId").exception();
              }

              validateSignature(samlCoreDataResponse.getSignature(), entityDescriptor);
              URI redirect = URI.create(secureStateData(relayState));

              return ssoService
                  .authorizeSamlSso(email, fullName, organizationId, redirect, serverBaseUri)
                  .exceptionally(
                      throwable -> ssoErrorAuthorizationResponse(throwable, domain, redirect));
            });
  }

  public void validateConfigurationEndpoint(URL configurationEndpoint) {
    retrieveMetadataCatchy(configurationEndpoint);
  }

  private URI buildAuthorizationURI(String state, URL metadataEndpoint) {
    try {
      SamlMetadataEntityDescriptor descriptor = retrieveMetadata(metadataEndpoint);
      return buildAuthorizationUri(state, descriptor);
    } catch (IOException | XMLParserException | UnmarshallingException ex) {
      log.error(
          "[AUTH]: SAML failed to build authorizationUri metadataEndpoint={}",
          metadataEndpoint,
          ex);
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  @Override
  public CompletionStage<SsoAuthorizationRequest> getSsoAuthorizationRequest(
      SsoSetupDTO ssoSetup, AuthorizationRequest request) {
    URL metadataEndpoint = ssoSetup.getSaml().getMetadataEndpoint();
    String state = secureState(request.getRedirect().toString());
    URI location = buildAuthorizationURI(state, metadataEndpoint);
    return CompletableFuture.completedStage(
        new SsoAuthorizationRequest(location, request.getDomain(), state));
  }
}
