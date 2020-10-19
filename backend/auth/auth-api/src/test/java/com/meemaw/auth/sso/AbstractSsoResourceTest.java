package com.meemaw.auth.sso;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.OAuthResource;
import com.meemaw.auth.sso.oauth.github.resource.v1.GithubOAuthResource;
import com.meemaw.auth.sso.oauth.google.resource.v1.GoogleOAuthResource;
import com.meemaw.auth.sso.oauth.microsoft.resource.v1.MicrosoftOAuthResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.test.setup.AbstractAuthApiTest;
import io.quarkus.test.common.http.TestHTTPResource;
import java.net.URI;
import javax.inject.Inject;

public abstract class AbstractSsoResourceTest extends AbstractAuthApiTest {

  public static final String SIMPLE_REDIRECT = "http://localhost:3000/test";

  @Inject protected AppConfig appConfig;
  @Inject protected UserDatasource userDatasource;
  @Inject protected TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @TestHTTPResource(MicrosoftOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI microsoftSignInURI;

  @TestHTTPResource(MicrosoftOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI microsoftCallbackURI;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI googleSignInURI;

  @TestHTTPResource(GoogleOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI googleCallbackURI;

  @TestHTTPResource(SamlResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI samlSignInURI;

  @TestHTTPResource(SamlResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI samlCallbackURI;

  @TestHTTPResource(GithubOAuthResource.PATH + "/" + OAuthResource.SIGNIN_PATH)
  protected URI githubSignInURI;

  @TestHTTPResource(GithubOAuthResource.PATH + "/" + OAuthResource.CALLBACK_PATH)
  protected URI githubCallbackURI;
}
