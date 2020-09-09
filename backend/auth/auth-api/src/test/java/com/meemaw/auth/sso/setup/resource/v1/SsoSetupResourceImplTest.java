package com.meemaw.auth.sso.setup.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static com.meemaw.test.setup.SsoTestSetupUtils.signUpAndLogin;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SsoSetupResourceImplTest {

  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;

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
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
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
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
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
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"method\":\"Required\",\"configurationEndpoint\":\"Required\"}}}"));
  }

  @Test
  public void setup__should_fail__when_broken_configuration_endpoint() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"saml\", \"configurationEndpoint\": \"random\"}")
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"Unprocessable Entity\",\"errors\":{\"configurationEndpoint\":\"Cannot deserialize value of type `java.net.URL` from String \\\"random\\\": not a valid textual representation, problem: no protocol: random\"}}}"));
  }

  @Test
  public void setup__should_fail__when_broken_method() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"random\", \"configurationEndpoint\": \"random\"}")
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"method\":\"Invalid Value\"}}}"));
  }

  @Test
  public void setup__should_fail__when_endpoint_is_down()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(SsoMethod.SAML, new URL("http://localhost:1000"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Connection refused\"}}}"));
  }

  @Test
  public void setup__should_fail__when_endpoint_does_not_serve_xml()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(SsoMethod.SAML, new URL("https://www.google.com"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Unable to parse inputstream, it contained invalid XML\"}}}"));
  }

  @Test
  public void setup__should_fail__when_endpoint_404()
      throws MalformedURLException, JsonProcessingException {
    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(
            SsoMethod.SAML,
            new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metada"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Not Found\"}}}"));
  }

  @Test
  public void setup__should_fail__when_non_business_email_is_used()
      throws MalformedURLException, JsonProcessingException {
    String sessionId =
        signUpAndLogin(
            mailbox, objectMapper, "sso-setup-regular-email@gmail.com", "sso-setup-regular-email");

    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(
            SsoMethod.SAML,
            new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup is only possible for work domain.\"}}"));
  }

  @Test
  public void setup__should_work__when_business_email_is_used()
      throws MalformedURLException, JsonProcessingException {
    String sessionId =
        signUpAndLogin(
            mailbox,
            objectMapper,
            "sso-setup-business-email@snuderls.io",
            "sso-setup-business-emai");

    CreateSsoSetupDTO body =
        new CreateSsoSetupDTO(
            SsoMethod.SAML,
            new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata"));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    // Should fail if already configured
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(JacksonMapper.get().writeValueAsString(body))
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"SSO setup already configured\"}}"));
  }
}
