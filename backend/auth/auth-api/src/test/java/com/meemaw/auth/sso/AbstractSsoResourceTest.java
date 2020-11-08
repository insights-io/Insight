package com.meemaw.auth.sso;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.github.resource.v1.GithubOAuthResource;
import com.meemaw.auth.sso.oauth.google.resource.v1.GoogleOAuthResource;
import com.meemaw.auth.sso.oauth.microsoft.resource.v1.MicrosoftOAuthResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.test.setup.AbstractAuthApiTest;
import io.quarkus.test.common.http.TestHTTPResource;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public abstract class AbstractSsoResourceTest extends AbstractAuthApiTest {

  public static final String SIMPLE_REDIRECT = "http://localhost:3000/test";

  public static URL oktaMetadataEndpoint() throws MalformedURLException {
    return new URL("https://snuderlstest.okta.com/app/exkligrqDovHJsGmk5d5/sso/saml/metadata");
  }

  @Inject protected AppConfig appConfig;
  @Inject protected TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @TestHTTPResource(MicrosoftOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI microsoftSignInURI;

  @TestHTTPResource(MicrosoftOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI microsoftCallbackURI;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI googleSignInURI;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI googleCallbackURI;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.SIGNIN_PATH)
  protected URI samlSignInURI;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.CALLBACK_PATH)
  protected URI samlCallbackURI;

  @TestHTTPResource(GithubOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI githubSignInURI;

  @TestHTTPResource(GithubOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI githubCallbackURI;

  protected String readFileAsString(String name) throws URISyntaxException, IOException {
    return Files.readString(Path.of(getClass().getResource(name).toURI()));
  }

  protected Pair<String, String> samlResponseForRandomDomain()
      throws IOException, URISyntaxException {
    String email = String.format("%s@%s.com", UUID.randomUUID(), UUID.randomUUID());
    String samlResponse = readFileAsString("/sso/saml/response/okta_matej_snuderls_eu.xml");
    samlResponse = samlResponse.replaceAll("matej\\.snuderl@snuderls\\.eu", email);
    return Pair.of(email, Base64.getEncoder().encodeToString(samlResponse.getBytes()));
  }
}
