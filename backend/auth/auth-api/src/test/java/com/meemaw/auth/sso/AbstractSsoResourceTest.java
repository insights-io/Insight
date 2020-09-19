package com.meemaw.auth.sso;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.oauth.github.resource.v1.OAuth2GithubResource;
import com.meemaw.auth.sso.oauth.google.resource.v1.OAuth2GoogleResource;
import com.meemaw.auth.sso.oauth.microsoft.resource.v1.OAuth2MicrosoftResource;
import com.meemaw.auth.sso.saml.resource.v1.SamlResource;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
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

  @TestHTTPResource(OAuth2MicrosoftResource.PATH + "/" + OAuth2Resource.SIGNIN_PATH)
  protected URI microsoftSignInURI;

  @TestHTTPResource(OAuth2MicrosoftResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  protected URI microsoftCallbackURI;

  @TestHTTPResource(OAuth2GoogleResource.PATH + "/" + OAuth2Resource.SIGNIN_PATH)
  protected URI googleSignInURI;

  @TestHTTPResource(OAuth2GoogleResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  protected URI googleCallbackURI;

  @TestHTTPResource(SamlResource.PATH + "/" + OAuth2Resource.SIGNIN_PATH)
  protected URI samlSignInURI;

  @TestHTTPResource(SamlResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  protected URI samlCallbackURI;

  @TestHTTPResource(OAuth2GithubResource.PATH + "/" + OAuth2Resource.SIGNIN_PATH)
  protected URI githubSignInURI;

  @TestHTTPResource(OAuth2GithubResource.PATH + "/" + OAuth2Resource.CALLBACK_PATH)
  protected URI githubCallbackURI;
}
