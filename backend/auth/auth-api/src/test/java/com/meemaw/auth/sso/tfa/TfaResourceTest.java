package com.meemaw.auth.sso.tfa;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.sso.tfa.totp.impl.TotpUtils;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.setup.SsoTestSetupUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class TfaResourceTest {

  @Inject UserDatasource userDatasource;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @Test
  public void list_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .get(TfaResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void list_tfa__should_return_empty_list__when_tfa_no_configured() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .get(TfaResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }

  @Test
  public void get_tfa__should_throw__when_invalid_method() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .get(TfaResource.PATH + "/random")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void get_tfa__should_throw__when_tfa_no_configured(String method) {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .get(String.join("/", TfaResource.PATH, method))
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa__should_throw__when_invalid_method(String method) {
    given()
        .when()
        .delete(String.join("/", TfaResource.PATH, method))
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_totp_tfa__should_throw__when_not_authenticated(String method) {
    given()
        .when()
        .delete(String.join("/", TfaResource.PATH, method))
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"totp", "sms"})
  public void delete_tfa__should_return_false__when_user_without_tfa(String method) {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .delete(String.join("/", TfaResource.PATH, method))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void delete_totp_tfa__should_return_true__when_user_with_tfa()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "user-tfa-delete-full-flow@gmail.com";
    String password = "user-tfa-delete-full-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret = tfaTotpSetupDatasource.getTotpSecret(userId).toCompletableFuture().join().get();
    int tfaCode = TotpUtils.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaChallengeCompleteDTO(tfaCode)))
        .post(TfaResource.PATH + "/totp/setup")
        .then()
        .statusCode(200);

    // 200 on GET
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(TfaResource.PATH + "/totp")
        .then()
        .statusCode(200);

    // 200 and true on GET
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(TfaResource.PATH + "/totp")
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    // 404 on GET after delete
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(TfaResource.PATH + "/totp")
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
