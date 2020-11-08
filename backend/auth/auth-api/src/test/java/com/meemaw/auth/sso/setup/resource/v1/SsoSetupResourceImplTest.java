package com.meemaw.auth.sso.setup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.AbstractSsoResourceTest;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.setup.model.SamlMethod;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.model.dto.SsoSetup;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.testconainers.pg.PostgresTestResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import javax.ws.rs.core.MediaType;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoSetupResourceImplTest extends AbstractSsoResourceTest {

  @Test
  public void delete__should_throw__when_unauthorized() {
    ssoSessionCookieTestCases(Method.DELETE, SsoSetupResource.PATH);
    ssoBearerTokenTestCases(Method.DELETE, SsoSetupResource.PATH);
  }

  @Test
  public void delete__should_throw_404__when_setup_does_not_exist() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .delete(SsoSetupResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void get_setup_by_domain__should_return_false__when_no_sso_setup() {
    given()
        .when()
        .get(SsoSetupResource.PATH + "/gmail.com")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void get_setup__should_fail__when_not_authenticated() {
    given()
        .when()
        .get(SsoSetupResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_setup__should_throw_404__when_no_setup() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .get(SsoSetupResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"That email or domain isn’t registered for SSO.\"}}"));
  }

  @Test
  public void setup__should_fail__when_not_authenticated() {
    given()
        .when()
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void setup__should_fail__when_invalid_content_type() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void setup__should_fail___when_no_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void setup__should_fail__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"method\":\"Required\"}}}"));
  }

  @Test
  public void setup__should_fail__when_broken_method() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"random\", \"configurationEndpoint\": \"random\"}")
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"method\":\"Invalid Value\"}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_saml_without_configuration()
      throws JsonProcessingException {
    String payload =
        objectMapper.writeValueAsString(new CreateSsoSetupParams(SsoMethod.SAML, null));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":\"Required\"}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_malformed_url() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"saml\", \"saml\": {\"metadataEndpoint\": \"random\"}}")
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"Unprocessable Entity\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Cannot deserialize value of type `java.net.URL` from String \\\"random\\\": not a valid textual representation, problem: no protocol: random\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_is_down()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            new SamlConfiguration(SamlMethod.CUSTOM, new URL("http://localhost:1000")));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Connection refused\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_does_not_serve_xml()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            new SamlConfiguration(SamlMethod.CUSTOM, new URL("https://www.google.com")));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Malformed XML\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_404()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            SamlConfiguration.okta(
                new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metada")));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Not Found\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_non_business_email_is_used()
      throws MalformedURLException, JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String sessionId = authApi().signUpAndLogin(password + "@gmail.com", password);

    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            SamlConfiguration.okta(
                new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata")));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup is only possible for work domain.\"}}"));
  }

  @Test
  public void sso_saml_setup__should_work__when_business_email_is_used()
      throws MalformedURLException, JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = password + "@snuderls5.io";
    String sessionId = authApi().signUpAndLogin(email, password);

    CreateSsoSetupParams body =
        new CreateSsoSetupParams(SsoMethod.SAML, SamlConfiguration.okta(oktaMetadataEndpoint()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    DataResponse<SsoSetup> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(SsoSetupResource.PATH)
            .as(new TypeRef<>() {});

    Assertions.assertEquals(
        oktaMetadataEndpoint(), dataResponse.getData().getSaml().getMetadataEndpoint());
    Assertions.assertEquals(SsoMethod.SAML, dataResponse.getData().getMethod());
    Assertions.assertEquals(EmailUtils.domainFromEmail(email), dataResponse.getData().getDomain());

    given()
        .when()
        .get(SsoSetupResource.PATH + "/" + EmailUtils.domainFromEmail(email))
        .then()
        .statusCode(200)
        .body(sameJson(String.format("{\"data\":\"%s\"}", samlSignInURI)));

    // Should fail if already configured
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup already configured\"}}"));
  }

  @Test
  public void sso_google_setup__should_work__when_business_email() throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = password + "@snuderls10.io";
    String sessionId = authApi().signUpAndLogin(email, password);
    CreateSsoSetupParams body = new CreateSsoSetupParams(SsoMethod.GOOGLE, null);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    given()
        .when()
        .get(SsoSetupResource.PATH + "/" + EmailUtils.domainFromEmail(email))
        .then()
        .statusCode(200)
        .body(sameJson(String.format("{\"data\":\"%s\"}", googleSignInURI)));

    // Should fail if already configured
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup already configured\"}}"));

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(SsoSetupResource.PATH)
        .then()
        .statusCode(204);
  }
}
