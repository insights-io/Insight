package com.rebrowse.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.test.utils.EmailTestUtils;
import com.rebrowse.test.utils.auth.AuthorizationFlows;
import com.rebrowse.test.utils.auth.SignUpFlows;
import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.net.RequestOptions;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTestResource(PostgresTestResource.class)
public abstract class AbstractAuthApiQuarkusTest {

  protected ObjectMapper clientObjectMapper = new ObjectMapper();

  @Inject protected MockMailbox mailbox;
  @Inject protected ObjectMapper objectMapper;

  @TestHTTPResource protected URI baseUri;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @Inject MockSmsbox mockSmsbox;
  @Inject AppConfig appConfig;

  @BeforeEach
  void init() {
    mailbox.clear();
  }

  public RequestOptions.Builder sdkRequest() {
    return new RequestOptions.Builder().apiBaseUrl(baseUri.toString());
  }

  public SignUpFlows signUpFlows() {
    return new SignUpFlows(
        baseUri,
        objectMapper,
        (email) -> {
          List<Mail> messages = mailbox.getMessagesSentTo(email);
          Mail lastMessage = messages.get(messages.size() - 1);
          return EmailTestUtils.parseLink(lastMessage);
        });
  }

  public UserFlows userFlows() {
    return new UserFlows(baseUri, objectMapper, mockSmsbox);
  }

  public PwdAuthorizationFlows pwdAuthorizationFlows() {
    return new PwdAuthorizationFlows(baseUri, objectMapper);
  }

  public AuthorizationSsoFlows authorizationSsoFlows() {
    return new AuthorizationSsoFlows(baseUri, objectMapper, appConfig);
  }

  public PasswordFlows passwordFlows() {
    return new PasswordFlows(baseUri, objectMapper);
  }

  public OrganizationFlows organizationFlows() {
    return new OrganizationFlows(baseUri, objectMapper);
  }

  public OAuthFlows oauthFlows() {
    return new OAuthFlows(baseUri, objectMapper);
  }

  public SsoSetupFlows ssoSetupFlows() {
    return new SsoSetupFlows(baseUri, objectMapper);
  }

  public AuthorizationFlows authorizationFlows() {
    return new AuthorizationFlows(baseUri, objectMapper);
  }

  public MfaSetupFlows mfaSetupFlows() {
    return new MfaSetupFlows(mockSmsbox, baseUri, issuer, objectMapper);
  }

  public MfaAuthorizationFlows mfaAuthorizationFlows() {
    return new MfaAuthorizationFlows(baseUri, mockSmsbox, objectMapper);
  }
}
