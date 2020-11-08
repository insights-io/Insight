package com.meemaw.auth.sso.saml.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.saml.model.SamlDataResponse;
import com.meemaw.auth.sso.saml.model.metadata.xml.EntityDescriptor;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Slf4j
public class SamlService extends AbstractIdentityProvider {

  private static final String EMAIL_CLAIMS =
      "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoService ssoService;
  @Inject ParseProvider parseProvider;

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.SAML;
  }

  @Override
  public String basePath() {
    return SamlResource.PATH;
  }

  public URI buildAuthorizationUri(String state, EntityDescriptor entityDescriptor) {
    return UriBuilder.fromUri(entityDescriptor.getHttpPostSignOnService().get().getLocation())
        .queryParam("RelayState", state)
        .build();
  }

  private void validateSignature(Signature signature, EntityDescriptor entityDescriptor) {
    try {
      SignatureValidator.validate(
          signature, parseProvider.certificate(entityDescriptor.getCertificate()));
    } catch (SignatureException | CertificateException ex) {
      log.error("[AUTH]: SAML callback signature exception", ex);
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  public void validateSignature(
      SamlDataResponse samlDataResponse, EntityDescriptor entityDescriptor) {

    validateSignature(samlDataResponse.getSignature(), entityDescriptor);
  }

  private EntityDescriptor retrieveMetadataCatchy(URL metadataEndpoint) {
    try {
      return retrieveMetadata(metadataEndpoint);
    } catch (FileNotFoundException ex) {
      throw retrieveMetadataException(metadataEndpoint, "Failed to retrieve: Not Found", ex);
    } catch (JsonParseException ex) {
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

  private EntityDescriptor retrieveMetadata(URL metadataEndpoint) throws IOException {
    InputStream is = metadataEndpoint.openConnection().getInputStream();
    return parseProvider.readEntityDescriptor(is);
  }

  private SamlDataResponse fromAssertion(Assertion assertion) {
    String email =
        assertion.getAttributeStatements().stream()
            .flatMap(as -> as.getAttributes().stream())
            .filter(at -> at.getName().equals(EMAIL_CLAIMS))
            .findFirst()
            .map(attr -> attr.getAttributeValues().get(0).getDOM().getTextContent())
            .get();

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
    Signature signature = assertion.getSignature();

    return new SamlDataResponse(email, fullName.orElse(null), issuer, signature);
  }

  public SamlDataResponse decodeSamlResponse(String samlResponse)
      throws UnmarshallingException, XMLParserException {
    Response openSamlResponse = decodeOpenSamlResponse(samlResponse);

    if (openSamlResponse.getAssertions().isEmpty()) {
      throw Boom.badRequest().message("Missing assertions").exception();
    }

    return fromAssertion(openSamlResponse.getAssertions().get(0));
  }

  private InputStream xmlSamlResponse(String samlResponse) {
    byte[] base64DecodedResponse = Base64.getDecoder().decode(samlResponse);
    System.out.println(new String(base64DecodedResponse));
    return new ByteArrayInputStream(base64DecodedResponse);
  }

  private Response decodeOpenSamlResponse(String samlResponse)
      throws UnmarshallingException, XMLParserException {
    System.out.println("SAML RESPONSE: " + samlResponse);

    InputStream is = xmlSamlResponse(samlResponse);
    Document document = parseProvider.parse(is);
    Element element = document.getDocumentElement();
    UnmarshallerFactory umFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    Unmarshaller unmarshaller = umFactory.getUnmarshaller(element);
    if (unmarshaller == null) {
      log.error("[AUTH]: Failed to get un-marshaller to decode SAMLResponse={}", samlResponse);
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
              URL configurationEndpoint = maybeSsoSetup.get().getSaml().getMetadataEndpoint();
              EntityDescriptor entityDescriptor = retrieveMetadataCatchy(configurationEndpoint);
              if (!entityDescriptor.getEntityId().equals(samlDataResponse.getIssuer())) {
                log.error(
                    "[AUTH]: SAML callback entity miss-match expected={} actual={} organization={}",
                    entityDescriptor.getEntityId(),
                    samlDataResponse.getIssuer(),
                    organizationId);
                throw Boom.badRequest().message("Invalid entityId").exception();
              }
              validateSignature(samlDataResponse.getSignature(), entityDescriptor);
              URL redirect = RequestUtils.sneakyURL(secureStateData(relayState));
              String cookieDomain = RequestUtils.parseCookieDomain(redirect);

              return ssoService
                  .ssoLogin(email, samlDataResponse.getFullName(), organizationId, redirect)
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
    } catch (IOException ex) {
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
