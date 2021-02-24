package com.rebrowse.auth.sso.saml.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.organization.datasource.OrganizationDatasource;
import com.rebrowse.auth.organization.model.CreateOrganizationParams;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.AbstractSsoResourceTest;
import com.rebrowse.auth.sso.saml.client.SamlClient;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.CreateSsoSetup;
import com.rebrowse.auth.sso.setup.model.dto.SamlConfigurationDTO;
import com.rebrowse.auth.utils.AuthApiTestData;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.auth.utils.MockedSamlClient;
import com.rebrowse.model.auth.SamlConfiguration;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserRole;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SamlResourceImplTest extends AbstractSsoResourceTest {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject OrganizationDatasource organizationDatasource;

  @Test
  public void saml_sign_in__should_fail__when_no_params() {
    given()
        .when()
        .get(samlSignInURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void saml_sign_in__should_fail__when_malformed_email() {
    given()
        .when()
        .queryParam("redirect", "http://localhost:3000/test")
        .queryParam("email", "matej.snuderl")
        .get(samlSignInURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void saml_sign_in__should_fail__when_malformed_redirect() {
    given()
        .when()
        .queryParam("redirect", "/test")
        .queryParam("email", "matej.snuderl@snuderls.eu")
        .get(samlSignInURI)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void saml_sign_in__should_fail__when_domain_with_no_sso_setup() {
    given()
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .queryParam("email", AuthApiTestUtils.randomBusinessEmail())
        .get(samlSignInURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"That email or domain isn’t registered for SSO.\"}}"));
  }

  @Test
  public void saml_sign_in__should_redirect_to_sso_provider__when_sso_setup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String email = authorizationFlows().retrieveUserData(sessionId).getUser().getEmail();

    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);
    ssoSetupFlows()
        .create(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);

    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .queryParam("email", email)
        .get(samlSignInURI)
        .then()
        .statusCode(302)
        .header(
            "Location", Matchers.matchesPattern(AuthApiTestData.OKTA_AUTHORIZE_ENDPOINT_PATTERN))
        .cookie(SsoAuthorizationSession.COOKIE_NAME);
  }

  @Test
  public void saml_callback__should_fail__when_no_params() {
    given()
        .when()
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"RelayState\":\"Required\",\"SAMLResponse\":\"Required\"}}}"));
  }

  @Test
  public void saml_callback__should_fail__on_random_saml_response() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));
    given()
        .when()
        .formParam("SAMLResponse", "random")
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid SAMLResponse\"}}"));
  }

  @Test
  public void saml_callback__should_fail__on_invalid_state_cookie() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .formParam("SAMLResponse", "random")
        .formParam("RelayState", state)
        .post(samlCallbackURI)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void saml_callback__should_fail__when_configuration_endpoint_is_down()
      throws IOException, URISyntaxException {
    String organizationId = Organization.identifier();
    organizationDatasource
        .create(new CreateOrganizationParams(organizationId, "Test"))
        .toCompletableFuture()
        .join();

    Pair<String, String> data = samlResponseForRandomDomain();
    String samlResponse = data.getRight();
    String email = data.getLeft();
    String domain = EmailUtils.getDomain(email);

    ssoSetupDatasource
        .create(
            CreateSsoSetup.saml(
                organizationId,
                domain,
                SamlConfigurationDTO.okta(GlobalTestData.LOCALHOST_REDIRECT_URL)))
        .toCompletableFuture()
        .join();

    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Connection refused\"}}}}"));
  }

  @Test
  public void saml_callback__should_fail__when_configuration_endpoint_serves_broken_xml()
      throws IOException, URISyntaxException {
    String organizationId = Organization.identifier();

    organizationDatasource
        .create(new CreateOrganizationParams(organizationId, "Test"))
        .toCompletableFuture()
        .join();

    Pair<String, String> data = samlResponseForRandomDomain();
    String samlResponse = data.getRight();
    String email = data.getLeft();
    String domain = EmailUtils.getDomain(email);

    ssoSetupDatasource
        .create(
            CreateSsoSetup.saml(
                organizationId, domain, SamlConfigurationDTO.okta(new URL("https://google.com"))))
        .toCompletableFuture()
        .join();

    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Malformed XML\"}}}}"));
  }

  @Test
  public void saml_callback__should_fail__when_configuration_endpoint_serves_invalid_xml()
      throws IOException, URISyntaxException {
    String organizationId = Organization.identifier();
    organizationDatasource
        .create(new CreateOrganizationParams(organizationId, "Test"))
        .toCompletableFuture()
        .join();

    Pair<String, String> data = samlResponseForRandomDomain();
    String samlResponse = data.getRight();
    String email = data.getLeft();
    String domain = EmailUtils.getDomain(email);

    ssoSetupDatasource
        .create(
            CreateSsoSetup.saml(
                organizationId,
                domain,
                SamlConfigurationDTO.okta(new URL("https://www.w3schools.com/xml/note.xml"))))
        .toCompletableFuture()
        .join();

    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Malformed XML\"}}}}"));
  }

  @Test
  public void saml_callback__should_sign_up_user__when_valid_saml_response()
      throws IOException, URISyntaxException {
    String email = "matej.snuderl@snuderls.eu";
    String sessionId = signUpFlows().signUpAndLogin(email, UUID.randomUUID().toString());
    String organizationId =
        organizationFlows()
            .update(OrganizationUpdateParams.builder().openMembership(true).build(), sessionId)
            .getId();
    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);

    // sso setup
    ssoSetupFlows()
        .create(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);

    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    String samlResponse =
        new String(
            Base64.getEncoder()
                .encode(
                    readFileAsString("/sso/saml/response/okta_matej_snuderls_eu.xml").getBytes()));

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(302)
        .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, "");

    String blazSnuderlSamlResponse =
        new String(
            Base64.getEncoder()
                .encode(
                    readFileAsString("/sso/saml/response/okta_blaz_snuderls_eu.xml").getBytes()));

    String newSessionId =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .formParam("SAMLResponse", blazSnuderlSamlResponse)
            .formParam("RelayState", state)
            .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
            .post(samlCallbackURI)
            .then()
            .statusCode(302)
            .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
            .cookie(SsoAuthorizationSession.COOKIE_NAME, "")
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    User newUser = authorizationFlows().retrieveUserData(newSessionId).getUser();

    assertEquals(newUser.getFullName(), "Blaz Snuderl");
    assertEquals(newUser.getOrganizationId(), organizationId);
    assertEquals(newUser.getRole(), UserRole.MEMBER);
  }

  @Test
  public void saml_callback__should_fail__when_invalid_signature()
      throws IOException, URISyntaxException {
    String organizationId = Organization.identifier();
    Pair<String, String> data = samlResponseForRandomDomain();
    String samlResponse = data.getRight();
    String email = data.getLeft();
    String domain = EmailUtils.getDomain(email);

    organizationDatasource
        .create(new CreateOrganizationParams(organizationId, "Test"))
        .toCompletableFuture()
        .join();

    ssoSetupDatasource
        .create(
            CreateSsoSetup.saml(
                organizationId,
                domain,
                SamlConfigurationDTO.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)))
        .toCompletableFuture()
        .join();

    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);
    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Signature cryptographic validation not successful\"}}"));
  }
}
