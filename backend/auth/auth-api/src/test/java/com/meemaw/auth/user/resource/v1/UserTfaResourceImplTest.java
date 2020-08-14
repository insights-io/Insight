package com.meemaw.auth.user.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
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

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class UserTfaResourceImplTest {

  @Inject UserDatasource userDatasource;
  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject SsoVerificationDatasource verificationDatasource;

  @Test
  public void get_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .get(UserTfaResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void get_tfa__should_throw__when_tfa_no_configured() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .get(UserTfaResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void delete_tfa__should_throw__when_not_authenticated() {
    given()
        .when()
        .delete(UserTfaResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void delete_tfa__should_return_false__when_user_without_tfa() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .delete(UserTfaResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":false}"));
  }

  @Test
  public void delete_tfa__should_return_true__when_user_with_tfa()
      throws JsonProcessingException, GeneralSecurityException {
    String email = "user-tfa-delete-full-flow@gmail.com";
    String password = "user-tfa-delete-full-flow";
    String sessionId = SsoTestSetupUtils.signUpAndLogin(mailbox, objectMapper, email, password);

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(200);

    UUID userId = userDatasource.findUser(email).toCompletableFuture().join().get().getId();
    String secret =
        verificationDatasource.getTfaSetupSecret(userId).toCompletableFuture().join().get();
    int tfaCode = (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(200);

    // 200 on GET
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(UserTfaResource.PATH)
        .then()
        .statusCode(200);

    // 200 and true on GET
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .delete(UserTfaResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":true}"));

    // 404 on GET after delete
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(UserTfaResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }
}
