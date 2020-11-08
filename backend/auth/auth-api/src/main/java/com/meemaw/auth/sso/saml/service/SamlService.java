package com.meemaw.auth.sso.saml.service;

import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.MDC;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.saml.model.SamlCoreDataResponse;
import com.meemaw.auth.sso.saml.model.SamlMetadataEntityDescriptor;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

@ApplicationScoped
@Slf4j
public class SamlService extends AbstractIdentityProvider {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoService ssoService;
  @Inject SamlParser samlParser;

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.SAML;
  }

  @Override
  public String basePath() {
    return SamlResource.PATH;
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
    InputStream is = metadataEndpoint.openConnection().getInputStream();
    return samlParser.parseSamlMetadataEntityDescriptor(is);
  }

  public CompletionStage<SsoLoginResult<?>> handleCallback(String samlResponse, String relayState) {
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

    String email = samlCoreDataResponse.getEmail();
    String domain = EmailUtils.domainFromEmail(email);

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
              URL redirect = RequestUtils.sneakyURL(secureStateData(relayState));
              String cookieDomain = RequestUtils.parseCookieDomain(redirect);

              return ssoService
                  .ssoLogin(email, samlCoreDataResponse.getFullName(), organizationId, redirect)
                  .exceptionally(throwable -> handleSsoException(throwable, redirect))
                  .thenApply(loginResult -> new SsoLoginResult<>(loginResult, cookieDomain));
            });
  }

  public void validateConfigurationEndpoint(URL configurationEndpoint) {
    retrieveMetadataCatchy(configurationEndpoint);
  }

  private URI buildAuthorizationURI(String state, URL metadataEndpoint) {
    try {
      return buildAuthorizationUri(state, retrieveMetadata(metadataEndpoint));
    } catch (IOException | XMLParserException | UnmarshallingException ex) {
      log.error("[AUTH]: SAML failed to build authorizationUri", ex);
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  public javax.ws.rs.core.Response signInRedirectResponse(
      URL configurationEndpoint, String cookieDomain, URL redirect) {
    String relayState = secureState(redirect.toString());
    URI location = buildAuthorizationURI(relayState, configurationEndpoint);
    return javax.ws.rs.core.Response.status(Status.FOUND)
        .cookie(SsoSignInSession.cookie(relayState, cookieDomain))
        .header("Location", location)
        .build();
  }
}
