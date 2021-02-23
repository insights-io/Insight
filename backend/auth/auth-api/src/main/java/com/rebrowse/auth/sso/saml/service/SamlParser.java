package com.rebrowse.auth.sso.saml.service;

import com.rebrowse.auth.sso.saml.model.SamlCoreDataResponse;
import com.rebrowse.auth.sso.saml.model.SamlMetadataEntityDescriptor;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.api.RebrowseApi;
import io.quarkus.runtime.StartupEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@ApplicationScoped
@Slf4j
public class SamlParser {

  private static final String SAML_EMAIL_CLAIMS =
      "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";

  private CertificateFactory certificateFactory;
  private BasicParserPool pool;

  public void init(@Observes StartupEvent event) {
    try {
      InitializationService.initialize();
      certificateFactory = CertificateFactory.getInstance("X.509");
      pool = new BasicParserPool();
      pool.initialize();
    } catch (InitializationException | CertificateException | ComponentInitializationException ex) {
      log.error("Failed to initialize", ex);
    }
  }

  public Credential certificate(String body) throws CertificateException {
    String certificate = "-----BEGIN CERTIFICATE-----\n" + body + "-----END CERTIFICATE-----";
    return new BasicX509Credential(
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(certificate.getBytes(RebrowseApi.CHARSET))));
  }

  private SamlCoreDataResponse fromAssertion(Assertion assertion) {
    String email =
        assertion.getAttributeStatements().stream()
            .flatMap(as -> as.getAttributes().stream())
            .filter(at -> at.getName().equals(SAML_EMAIL_CLAIMS))
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

    return new SamlCoreDataResponse(email, fullName.orElse(null), issuer, signature);
  }

  private InputStream base64decode(String value) {
    byte[] base64DecodedResponse = Base64.getDecoder().decode(value);
    return new ByteArrayInputStream(base64DecodedResponse);
  }

  private <T> T parseXML(InputStream is) throws XMLParserException, UnmarshallingException {
    Document document = pool.parse(is);
    Element element = document.getDocumentElement();
    UnmarshallerFactory umFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    Unmarshaller unmarshaller = umFactory.getUnmarshaller(element);
    if (unmarshaller == null) {
      log.error("[AUTH]: Failed to get un-marshaller to decode element={}", element);
      throw Boom.badRequest()
          .errors(Map.of("saml", Map.of("metadataEndpoint", "Failed to retrieve: Malformed XML")))
          .exception();
    }

    return (T) unmarshaller.unmarshall(element);
  }

  public SamlMetadataEntityDescriptor parseSamlMetadataEntityDescriptor(InputStream is)
      throws UnmarshallingException, XMLParserException {
    EntityDescriptor entityDescriptor = parseXML(is);
    return new SamlMetadataEntityDescriptor(entityDescriptor);
  }

  public SamlCoreDataResponse parseSamlCoreResponse(String samlResponse)
      throws UnmarshallingException, XMLParserException {
    Response response = parseXML(base64decode(samlResponse));
    if (response.getAssertions().isEmpty()) {
      throw Boom.badRequest().message("Missing assertions").exception();
    }
    return fromAssertion(response.getAssertions().get(0));
  }
}
