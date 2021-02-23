package com.rebrowse.auth.sso;

import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.oauth.OAuthResource;
import com.rebrowse.auth.sso.oauth.github.resource.v1.GithubOAuthResource;
import com.rebrowse.auth.sso.oauth.google.resource.v1.GoogleOAuthResource;
import com.rebrowse.auth.sso.oauth.microsoft.resource.v1.MicrosoftOAuthResource;
import com.rebrowse.auth.sso.saml.resource.v1.SamlResource;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import io.quarkus.test.common.http.TestHTTPResource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

public abstract class AbstractSsoResourceTest extends AbstractAuthApiQuarkusTest {

  @Inject protected AppConfig appConfig;

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
