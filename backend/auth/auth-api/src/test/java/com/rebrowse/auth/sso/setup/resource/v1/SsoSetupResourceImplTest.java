package com.rebrowse.auth.sso.setup.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static com.rebrowse.test.utils.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.rebrowse.test.utils.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.saml.client.SamlClient;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.setup.model.SamlMethod;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import com.rebrowse.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.rebrowse.auth.sso.setup.model.dto.SamlConfigurationDTO;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestData;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import com.rebrowse.auth.utils.MockedSamlClient;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.model.auth.SamlConfiguration;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SsoSetupResourceImplTest extends AbstractAuthApiQuarkusTest {

  @Test
  public void delete__should_throw__when_unauthorized() {
    ssoSessionCookieTestCases(Method.DELETE, SsoSetupResource.PATH);
    ssoBearerTokenTestCases(Method.DELETE, SsoSetupResource.PATH);
  }

  @Test
  public void delete__should_throw_404__when_setup_does_not_exist() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(SsoSetupResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void setup__should_fail___when_no_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void setup__should_fail__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{}")
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"method\":\"Required\"}}}"));
  }

  @Test
  public void setup__should_fail__when_broken_method() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"random\", \"configurationEndpoint\": \"random\"}")
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    String payload =
        objectMapper.writeValueAsString(new CreateSsoSetupParams(SsoMethod.SAML, null));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":\"Required\"}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_malformed_url() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"method\": \"saml\", \"saml\": {\"metadataEndpoint\": \"random\"}}")
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(422)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":422,\"reason\":\"Unprocessable Entity\",\"message\":\"Unprocessable Entity\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Cannot deserialize value of type `java.net.URL` from String \\\"random\\\": not a valid textual representation, problem: no protocol: random\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_is_down() throws JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            new SamlConfigurationDTO(SamlMethod.CUSTOM, GlobalTestData.LOCALHOST_REDIRECT_URL));

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
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Connection refused\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_does_not_serve_xml()
      throws MalformedURLException, JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            new SamlConfigurationDTO(SamlMethod.CUSTOM, new URL("https://www.google.com")));

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
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Malformed XML\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_endpoint_404()
      throws MalformedURLException, JsonProcessingException {
    String sessionId = authorizationFlows().loginWithAdminUser();
    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            SamlConfigurationDTO.okta(
                new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metada")));

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
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"saml\":{\"metadataEndpoint\":\"Failed to retrieve: Not Found\"}}}}"));
  }

  @Test
  public void sso_saml_setup__should_fail__when_non_business_email_is_used()
      throws MalformedURLException, JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String sessionId = signUpFlows().signUpAndLogin(password + "@gmail.com", password);

    CreateSsoSetupParams body =
        new CreateSsoSetupParams(
            SsoMethod.SAML,
            SamlConfigurationDTO.okta(
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
  public void get_sso_saml_setup__should_work__when_business_email_is_used()
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = AuthApiTestUtils.randomBusinessEmail();
    String sessionId = signUpFlows().signUpAndLogin(email, password);

    QuarkusMock.installMockForType(MockedSamlClient.okta(), SamlClient.class);
    ssoSetupFlows()
        .create(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);

    DataResponse<SsoSetupDTO> dataResponse =
        given()
            .when()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .get(SsoSetupResource.PATH)
            .as(new TypeRef<>() {});

    Assertions.assertEquals(
        AuthApiTestData.OKTA_METADATA_ENDPOINT,
        dataResponse.getData().getSaml().getMetadataEndpoint());
    Assertions.assertEquals(SsoMethod.SAML, dataResponse.getData().getMethod());
    Assertions.assertEquals(EmailUtils.getDomain(email), dataResponse.getData().getDomain());

    // Should fail if already configured
    ssoSetupFlows()
        .createAlreadyExists(
            SsoSetupCreateParams.saml(
                SamlConfiguration.okta(AuthApiTestData.OKTA_METADATA_ENDPOINT)),
            sessionId);
  }

  @Test
  public void as_a_user_i_want_to_create_and_delete_google_sso_setup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();

    // Success
    ssoSetupFlows().create(SsoSetupCreateParams.google(), sessionId);

    // Failure
    ssoSetupFlows().createAlreadyExists(SsoSetupCreateParams.google(), sessionId);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(SsoSetupResource.PATH)
        .then()
        .statusCode(204);
  }
}
