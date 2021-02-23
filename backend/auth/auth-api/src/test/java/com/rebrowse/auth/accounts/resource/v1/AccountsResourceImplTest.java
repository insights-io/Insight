package com.rebrowse.auth.accounts.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.sso.saml.client.SamlClient;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestData;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.auth.utils.MockedSamlClient;
import com.rebrowse.model.auth.SamlConfiguration;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import com.rebrowse.test.utils.GlobalTestData;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class AccountsResourceImplTest extends AbstractAuthApiQuarkusTest {

  @TestHTTPResource(AccountsResource.PATH + "/choose")
  protected URL chooseAccountEndpoint;

  @Test
  public void choose_account__should_throw__when_missing_data() {
    given()
        .when()
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void choose_account__should_throw__when_invalid_redirect() {
    given()
        .when()
        .queryParam("redirect", "random")
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void choose_account__should_throw__when_missing_email() {
    given()
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"Required\"}}}"));
  }

  @Test
  public void choose_account__should_throw__when_email_no_redirect() {
    given()
        .when()
        .formParam("email", AuthApiTestUtils.randomBusinessEmail())
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\"}}}"));
  }

  @Test
  public void choose_account__should_throw__when_invalid_email() {
    given()
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .formParam("email", UUID.randomUUID())
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void choose_account__should_not_leak_users__when_random_email() {
    authorizationFlows().chooseAccount(AuthApiTestUtils.randomBusinessEmail());
  }

  @Test
  public void choose_account__should_redirect_to_okta_sign_in__when_sso_saml_setup()
      throws JsonProcessingException {
    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();
    ssoSetupFlows()
        .create(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);
    authorizationFlows()
        .chooseAccountRedirectToSso(email, AuthApiTestData.OKTA_AUTHORIZE_ENDPOINT_PATTERN);
  }

  @Test
  public void choose_account__should_redirect_to_google_sign_in__when_sso_google_setup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();
    ssoSetupFlows().create(SsoSetupCreateParams.google(), sessionId);
    authorizationFlows()
        .chooseAccountRedirectToSso(
            email, authorizationSsoFlows().googleAuthorizationPattern(email));
  }

  @Test
  public void choose_account__should_redirect_to_github_sign_in__when_sso_github_setup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();
    ssoSetupFlows().create(SsoSetupCreateParams.github(), sessionId);
    authorizationFlows()
        .chooseAccountRedirectToSso(
            email, authorizationSsoFlows().githubAuthorizationPattern(email));
  }

  @Test
  public void choose_account__should_redirect_to_microsoft_sign_in__when_sso_microsoft_setup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();
    ssoSetupFlows().create(SsoSetupCreateParams.microsoft(), sessionId);
    authorizationFlows()
        .chooseAccountRedirectToSso(
            email, authorizationSsoFlows().microsoftAuthorizationPattern(email));
  }
}
