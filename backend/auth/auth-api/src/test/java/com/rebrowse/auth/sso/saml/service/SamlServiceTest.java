package com.rebrowse.auth.sso.saml.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rebrowse.auth.sso.saml.model.SamlCoreDataResponse;
import com.rebrowse.auth.sso.saml.model.SamlMetadataEntityDescriptor;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.inject.Inject;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.UnmarshallingException;

@QuarkusTest
@Tag("integration")
public class SamlServiceTest {

  @Inject SamlParser samlParser;
  @Inject SamlService samlService;

  private String readFileAsString(String path) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(path).toURI()));
  }

  private InputStream readFileAsInputStream(String path) throws URISyntaxException, IOException {
    return Files.newInputStream(Path.of(getClass().getResource(path).toURI()));
  }

  @Test
  public void saml_service__should_validate_signature__when_okta_parsed()
      throws URISyntaxException, IOException, XMLParserException, UnmarshallingException {
    String samlResponse =
        new String(
            Base64.getEncoder()
                .encode(
                    readFileAsString("/sso/saml/response/okta_matej_snuderls_eu.xml").getBytes()));

    SamlCoreDataResponse dataResponse = samlParser.parseSamlCoreResponse(samlResponse);

    assertEquals("matej.snuderl@snuderls.eu", dataResponse.getEmail());
    assertEquals("http://www.okta.com/exkligrqDovHJsGmk5d5", dataResponse.getIssuer());
    assertEquals("Matej Snuderl", dataResponse.getFullName());

    SamlMetadataEntityDescriptor entityDescriptor =
        samlParser.parseSamlMetadataEntityDescriptor(
            readFileAsInputStream("/sso/saml/metadata/okta_metadata.xml"));

    samlService.validateSignature(dataResponse, entityDescriptor);
  }

  @Test
  public void saml_service__should_validate_signature__when_auth0_parsed()
      throws URISyntaxException, IOException, XMLParserException, UnmarshallingException {
    String samlResponse =
        new String(
            Base64.getEncoder()
                .encode(
                    readFileAsString("/sso/saml/response/auth0_matej_snuderls_eu.xml").getBytes()));

    SamlCoreDataResponse dataResponse = samlParser.parseSamlCoreResponse(samlResponse);

    assertEquals("matej.snuderl@snuderls.eu", dataResponse.getEmail());
    assertEquals("urn:dev-p4ltuwq1.us.auth0.com", dataResponse.getIssuer());
    assertNull(dataResponse.getFullName());

    SamlMetadataEntityDescriptor entityDescriptor =
        samlParser.parseSamlMetadataEntityDescriptor(
            readFileAsInputStream("/sso/saml/metadata/auth0_metadata.xml"));

    samlService.validateSignature(dataResponse, entityDescriptor);
  }
}
