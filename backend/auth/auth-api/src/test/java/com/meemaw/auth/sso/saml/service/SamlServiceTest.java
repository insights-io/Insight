package com.meemaw.auth.sso.saml.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.meemaw.auth.sso.saml.model.SamlDataResponse;
import com.meemaw.auth.sso.saml.model.metadata.xml.EntityDescriptor;
import io.quarkus.test.junit.QuarkusTest;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.UnmarshallingException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@Tag("integration")
public class SamlServiceTest {

  private static final XmlMapper xmlMapper = new XmlMapper();

  @Inject SamlService samlService;

  private String readSamlResponse(String path) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(path).toURI()));
  }

  @Test
  public void saml_service__should_decode_saml_response__when_okta()
      throws URISyntaxException, IOException, XMLParserException, UnmarshallingException {
    String samlResponse = readSamlResponse("/sso/saml/response/okta_response.txt");
    SamlDataResponse dataResponse = samlService.decodeSamlResponse(samlResponse);

    assertEquals("matej.snuderl@snuderls.eu", dataResponse.getEmail());
    assertEquals("http://www.okta.com/exkligrqDovHJsGmk5d5", dataResponse.getIssuer());
    assertEquals("Matej Snuderl", dataResponse.getFullName());

    EntityDescriptor entityDescriptor =
        xmlMapper.readValue(
            Files.newInputStream(
                Path.of(getClass().getResource("/sso/saml/metadata/okta_metadata.xml").toURI())),
            EntityDescriptor.class);

    samlService.validateSignature(dataResponse, entityDescriptor);
  }

  @Test
  public void saml_service__should_decode_saml_response__when_auth0()
      throws URISyntaxException, IOException, XMLParserException, UnmarshallingException {
    String samlResponse = readSamlResponse("/sso/saml/response/auth0_response.txt");
    SamlDataResponse dataResponse = samlService.decodeSamlResponse(samlResponse);

    assertEquals("matej.snuderl@snuderls.eu", dataResponse.getEmail());
    assertEquals("urn:dev-p4ltuwq1.us.auth0.com", dataResponse.getIssuer());
    assertNull(dataResponse.getFullName());

    EntityDescriptor entityDescriptor =
        xmlMapper.readValue(
            Files.newInputStream(
                Path.of(getClass().getResource("/sso/saml/metadata/auth0_metadata.xml").toURI())),
            EntityDescriptor.class);

    samlService.validateSignature(dataResponse, entityDescriptor);
  }
}
