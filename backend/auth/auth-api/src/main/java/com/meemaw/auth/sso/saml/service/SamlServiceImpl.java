package com.meemaw.auth.sso.saml.service;

import com.meemaw.auth.sso.AbstractIdentityProviderService;
import com.meemaw.auth.sso.saml.model.SamlMetadataResponse;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.runtime.StartupEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@ApplicationScoped
@Slf4j
public class SamlServiceImpl extends AbstractIdentityProviderService {

  private CertificateFactory certificateFactory;

  public void init(@Observes StartupEvent event) {
    try {
      InitializationService.initialize();
      certificateFactory = CertificateFactory.getInstance("X.509");
    } catch (InitializationException | CertificateException ex) {
      log.error("Failed to initialize", ex);
    }
  }

  public Credential credentials(SamlMetadataResponse metadata) throws CertificateException {
    X509Certificate certificate =
        (X509Certificate)
            certificateFactory.generateCertificate(
                new ByteArrayInputStream(metadata.getCertificate().getBytes()));
    return new BasicX509Credential(certificate);
  }

  public SamlMetadataResponse fetchMetadata(URL metadataURL)
      throws IOException, ParserConfigurationException, SAXException {
    InputStream is = metadataURL.openConnection().getInputStream();

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(is);
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
  }

  public Response decodeSamlResponse(String samlResponse)
      throws ParserConfigurationException, IOException, SAXException, UnmarshallingException {
    byte[] base64DecodedResponse = Base64.getDecoder().decode(samlResponse);
    ByteArrayInputStream is = new ByteArrayInputStream(base64DecodedResponse);
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = docBuilder.parse(is);
    Element element = document.getDocumentElement();

    UnmarshallerFactory umFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    Unmarshaller unmarshaller = umFactory.getUnmarshaller(element);
    if (unmarshaller == null) {
      log.error("[AUTH]: Failed to get unmarshaller to decode SAMLResponse={}", samlResponse);
      throw Boom.serverError().exception();
    }
    XMLObject responseXmlObj = unmarshaller.unmarshall(element);
    return (Response) responseXmlObj;
  }
}
