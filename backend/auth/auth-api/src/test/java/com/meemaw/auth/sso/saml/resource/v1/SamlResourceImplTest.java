package com.meemaw.auth.sso.saml.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.CreateOrganizationParams;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.organization.resource.v1.OrganizationResource;
import com.meemaw.auth.sso.AbstractIdentityProvider;
import com.meemaw.auth.sso.AbstractSsoResourceTest;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserRole;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SamlResourceImplTest extends AbstractSsoResourceTest {

  @Inject OrganizationDatasource organizationDatasource;
  @Inject SsoSetupDatasource ssoSetupDatasource;

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
        .queryParam("redirect", SIMPLE_REDIRECT)
        .queryParam("email", "matej.snuderl@snuderls.iooooo")
        .get(samlSignInURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"That email or domain isn’t registered for SSO.\"}}"));
  }

  @Test
  public void saml_sign_in__should_redirect_to_sso_provider__when_sso_setup()
      throws MalformedURLException {
    String email = "matej.snuderl@snuderls.mo";

    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .create(new CreateOrganizationParams(organizationId, "Test"))
            .toCompletableFuture()
            .join();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                SamlConfiguration.okta(oktaMetadataEndpoint())))
        .toCompletableFuture()
        .join();

    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .queryParam("redirect", SIMPLE_REDIRECT)
        .queryParam("email", email)
        .get(samlSignInURI)
        .then()
        .statusCode(302)
        .header(
            "Location",
            Matchers.matchesPattern(
                "^https:\\/\\/snuderlstest\\.okta\\.com\\/app\\/snuderlsorg2948061_rebrowse_2\\/exkligrqDovHJsGmk5d5\\/sso\\/saml\\?RelayState=(.*)http%3A%2F%2Flocalhost%3A3000%2Ftest$"))
        .cookie(SsoSignInSession.COOKIE_NAME);
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
            URLEncoder.encode(SIMPLE_REDIRECT, RebrowseApi.CHARSET));
    given()
        .when()
        .formParam("SAMLResponse", "random")
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
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
            URLEncoder.encode(SIMPLE_REDIRECT, RebrowseApi.CHARSET));

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
    String email = data.getLeft();
    String samlResponse = data.getRight();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organizationId,
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                SamlConfiguration.okta(new URL("http://localhost:1000"))))
        .toCompletableFuture()
        .join();

    String location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
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
    String email = data.getLeft();
    String samlResponse = data.getRight();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organizationId,
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                SamlConfiguration.okta(new URL("https://google.com"))))
        .toCompletableFuture()
        .join();

    String location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
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
    Organization organization =
        organizationDatasource
            .create(new CreateOrganizationParams(organizationId, "Test"))
            .toCompletableFuture()
            .join();

    Pair<String, String> data = samlResponseForRandomDomain();
    String email = data.getLeft();
    String samlResponse = data.getRight();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                SamlConfiguration.okta(new URL("https://www.w3schools.com/xml/note.xml"))))
        .toCompletableFuture()
        .join();

    String location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
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
    String sessionId = authApi().signUpAndLogin(email, UUID.randomUUID().toString());

    // open membership setup
    DataResponse<OrganizationDTO> dataResponse =
        given()
            .when()
            .contentType(ContentType.JSON)
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .body(objectMapper.writeValueAsString(Map.of("openMembership", true)))
            .patch(OrganizationResource.PATH)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {});

    // sso setup
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new CreateSsoSetupParams(
                    SsoMethod.SAML, SamlConfiguration.okta(oktaMetadataEndpoint()))))
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(location);
    String samlResponse =
        new String(
            Base64.getEncoder()
                .encode(
                    readFileAsString("/sso/saml/response/okta_matej_snuderls_eu.xml").getBytes()));

    String organizationId = dataResponse.getData().getId();
    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(302)
        .header("Location", location)
        .cookie(SsoSession.COOKIE_NAME);

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
            .cookie(SsoSignInSession.COOKIE_NAME, state)
            .post(samlCallbackURI)
            .then()
            .statusCode(302)
            .header("Location", location)
            .extract()
            .detailedCookie(SsoSession.COOKIE_NAME)
            .getValue();

    User newUser =
        UserData.retrieve(authApi().sdkRequest().sessionId(newSessionId).build())
            .toCompletableFuture()
            .join()
            .getUser();

    assertEquals(newUser.getFullName(), "Blaz Snuderl");
    assertEquals(newUser.getOrganizationId(), organizationId);
    assertEquals(newUser.getRole(), UserRole.MEMBER);
  }

  @Test
  public void saml_callback__should_fail__when_invalid_signature()
      throws IOException, URISyntaxException {
    String organizationId = Organization.identifier();
    Pair<String, String> data = samlResponseForRandomDomain();
    String email = data.getLeft();
    String samlResponse = data.getRight();

    organizationDatasource
        .create(new CreateOrganizationParams(organizationId, "Test"))
        .toCompletableFuture()
        .join();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organizationId,
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                SamlConfiguration.okta(oktaMetadataEndpoint())))
        .toCompletableFuture()
        .join();

    String location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(location);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie(SsoSignInSession.COOKIE_NAME, state)
        .post(samlCallbackURI)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Signature cryptographic validation not successful\"}}"));
  }
}
