package com.meemaw.auth.sso.saml.service;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.AbstractIdpService;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.saml.model.SamlDataResponse;
import com.meemaw.auth.sso.saml.model.SamlMetadataResponse;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.runtime.StartupEvent;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ApplicationScoped
@Slf4j
public class SamlService extends AbstractIdpService {

  private CertificateFactory certificateFactory;
  private BasicParserPool parsePool;

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoService ssoService;

  public void init(@Observes StartupEvent event) {
    try {
      InitializationService.initialize();
      certificateFactory = CertificateFactory.getInstance("X.509");
      parsePool = new BasicParserPool();
      parsePool.initialize();
    } catch (InitializationException | CertificateException | ComponentInitializationException ex) {
      log.error("Failed to initialize", ex);
    }
  }

  /**
   * Extract Credential (BasicX509Credential) from SAML metadata response.
   *
   * @param metadata saml metadata response
   * @return Credential
   * @throws CertificateException if failed to generate certificate
   */
  public Credential credentials(SamlMetadataResponse metadata) throws CertificateException {
    X509Certificate certificate =
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(metadata.getCertificate().getBytes()));
    return new BasicX509Credential(certificate);
  }

  public URI buildAuthorizationUri(String state, SamlMetadataResponse metadata) {
    return UriBuilder.fromUri(metadata.getSsoHttpPostBinding())
        .queryParam("RelayState", state)
        .build();
  }

  public void validateSignature(Signature signature, SamlMetadataResponse metadata) {
    try {
      Credential credential = credentials(metadata);
      SignatureValidator.validate(signature, credential);
    } catch (SignatureException ex) {
      log.error("[AUTH]: SAML callback signature exception metadata={}", metadata, ex);
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    } catch (CertificateException ex) {
      log.error("[AUTH]: SAML callback certificate exception metadata={}", metadata, ex);
      throw Boom.serverError().message(ex.getMessage()).exception(ex);
    }
  }

  private SamlMetadataResponse fetchMetadataSneaky(URL metadataURL) {
    try {
      return fetchMetadata(metadataURL);
    } catch (FileNotFoundException ex) {
      log.error("[AUTH]: Failed to fetch SSO configuration", ex);
      throw failedToFetchSsoConfiguration(ex, "Not Found");
    } catch (IOException | XMLParserException ex) {
      log.error("[AUTH]: Failed to fetch SSO configuration", ex);
      throw failedToFetchSsoConfiguration(ex, ex.getMessage());
    }
  }

  public SamlMetadataResponse fetchMetadata(URL metadataURL)
      throws IOException, XMLParserException {
    InputStream is = metadataURL.openConnection().getInputStream();
    Document document = parsePool.parse(is);
    try {
      String certificate =
          "-----BEGIN CERTIFICATE-----\n"
              + document.getElementsByTagName("ds:X509Certificate").item(0).getTextContent()
              + "-----END CERTIFICATE-----";

      String entityId =
          document
              .getElementsByTagName("md:EntityDescriptor")
              .item(0)
              .getAttributes()
              .getNamedItem("entityID")
              .getNodeValue();

      NodeList singleSignOnServices = document.getElementsByTagName("md:SingleSignOnService");
      String ssoHttpPostBinding =
          singleSignOnServices.item(0).getAttributes().getNamedItem("Location").getNodeValue();
      String ssoHttpRedirectBinding =
          singleSignOnServices.item(1).getAttributes().getNamedItem("Location").getNodeValue();

      return new SamlMetadataResponse(
          certificate, ssoHttpPostBinding, ssoHttpRedirectBinding, entityId);
    } catch (NullPointerException ex) {
      throw new XMLParserException("Invalid XML");
    }
  }

  public SamlDataResponse decodeSamlResponse(String samlResponse)
      throws UnmarshallingException, XMLParserException {
    Response openSamlResponse = decodeOpenSamlResponse(samlResponse);
    if (openSamlResponse.getAssertions().isEmpty()) {
      throw Boom.badRequest().message("Missing assertions").exception();
    }

    Assertion assertion = openSamlResponse.getAssertions().get(0);
    String subject = assertion.getSubject().getNameID().getValue();
    Optional<String> givenName =
        assertion.getAttributeStatements().stream()
            .flatMap(as -> as.getAttributes().stream())
            .filter(attribute -> attribute.getName().equals("givenName"))
            .findFirst()
            .flatMap(
                attr ->
                    Optional.ofNullable(attr.getAttributeValues().get(0).getDOM())
                        .map(Node::getTextContent));

    Optional<String> familyName =
        assertion.getAttributeStatements().stream()
            .flatMap(as -> as.getAttributes().stream())
            .filter(attribute -> attribute.getName().equals("familyName"))
            .findFirst()
            .flatMap(
                attr ->
                    Optional.ofNullable(attr.getAttributeValues().get(0).getDOM())
                        .map(Node::getTextContent));

    Optional<String> fullName = givenName.flatMap(s -> familyName.map(s1 -> s + " " + s1));
    String issuer = assertion.getIssuer().getValue();
    Signature signature = openSamlResponse.getSignature();
    return new SamlDataResponse(subject, fullName.orElse(null), issuer, signature);
  }

  private Response decodeOpenSamlResponse(String samlResponse)
      throws UnmarshallingException, XMLParserException {
    byte[] base64DecodedResponse = Base64.getDecoder().decode(samlResponse);
    ByteArrayInputStream is = new ByteArrayInputStream(base64DecodedResponse);
    Document document = parsePool.parse(is);
    Element element = document.getDocumentElement();
    UnmarshallerFactory umFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    Unmarshaller unmarshaller = umFactory.getUnmarshaller(element);
    if (unmarshaller == null) {
      log.error("[AUTH]: Failed to get unmarshaller to decode SAMLResponse={}", samlResponse);
      throw Boom.serverError().exception();
    }
    return (Response) unmarshaller.unmarshall(element);
  }

  public CompletionStage<SsoLoginResult<?>> handleCallback(String samlResponse, String relayState) {
    SamlDataResponse samlDataResponse;
    try {
      samlDataResponse = decodeSamlResponse(samlResponse);
    } catch (UnmarshallingException | XMLParserException ex) {
      log.error("[AUTH]: Failed to decode SAMLResponse={}", samlResponse, ex);
      throw Boom.badRequest().message("Invalid SAMLResponse").exception(ex);
    }

    if (samlDataResponse.getSignature() == null) {
      log.error("[AUTH]: SAML callback missing signature SAMLResponse={}", samlResponse);
      throw Boom.badRequest().message("Missing signature").exception();
    }

    String email = samlDataResponse.getEmail();
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
              URL configurationEndpoint = maybeSsoSetup.get().getConfigurationEndpoint();
              SamlMetadataResponse samlMetadata = fetchMetadataSneaky(configurationEndpoint);
              if (!samlMetadata.getEntityId().equals(samlDataResponse.getIssuer())) {
                log.error(
                    "[AUTH]: SAML callback entity miss-match expected={} actual={} organization={}",
                    samlMetadata.getEntityId(),
                    samlDataResponse.getIssuer(),
                    organizationId);
                throw Boom.badRequest().message("Invalid entityId").exception();
              }
              validateSignature(samlDataResponse.getSignature(), samlMetadata);
              URL redirect = RequestUtils.sneakyURL(secureStateData(relayState));
              String cookieDomain = RequestUtils.parseCookieDomain(redirect);

              return ssoService
                  .ssoLogin(email, samlDataResponse.getFullName(), organizationId, redirect)
                  .exceptionally(throwable -> handleSsoException(throwable, redirect))
                  .thenApply(loginResult -> new SsoLoginResult<>(loginResult, cookieDomain));
            });
  }

  public void validateConfigurationEndpoint(URL configurationEndpoint) {
    fetchMetadataSneaky(configurationEndpoint);
  }

  private BoomException failedToFetchSsoConfiguration(Exception ex, String message) {
    return Boom.badRequest()
        .message("Failed to fetch SSO configuration")
        .errors(Map.of("configurationEndpoint", message))
        .exception(ex);
  }

  private URI buildAuthorizationURI(String state, URL configurationEndpoint) {
    try {
      SamlMetadataResponse metadata = fetchMetadata(configurationEndpoint);
      return buildAuthorizationUri(state, metadata);
    } catch (IOException | XMLParserException ex) {
      log.error("[AUTH]: SAML failed to build authorizationUri", ex);
      throw Boom.badRequest()
          .errors(Map.of("configurationEndpoint", ex.getMessage()))
          .exception(ex);
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

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.SAML;
  }

  @Override
  public String basePath() {
    return SamlResource.PATH;
  }
}
