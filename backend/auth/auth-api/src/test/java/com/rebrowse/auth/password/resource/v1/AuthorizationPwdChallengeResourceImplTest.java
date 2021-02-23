package com.rebrowse.auth.password.resource.v1;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.accounts.model.challenge.PwdChallengeResponseDTO;
import com.rebrowse.auth.utils.AbstractAuthApiQuarkusTest;
import com.rebrowse.auth.utils.AuthApiTestUtils;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class AuthorizationPwdChallengeResourceImplTest extends AbstractAuthApiQuarkusTest {

  @TestHTTPResource(AuthorizationPwdChallengeResource.PATH)
  protected URL completePasswordChallengeEndpoint;

  @Test
  public void get_password_challenge__should_throw__when_random_id() {
    given()
        .when()
        .get(completePasswordChallengeEndpoint + "/" + UUID.randomUUID())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void get_password_challenge__should_return_challenge__when_existing_id()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String challengeId = authorizationFlows().chooseAccount(email);
    given()
        .when()
        .get(completePasswordChallengeEndpoint + "/" + challengeId)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                objectMapper.writeValueAsString(
                    new RebrowseApiDataResponse<>(
                        new PwdChallengeResponseDTO(
                            GlobalTestData.LOCALHOST_REDIRECT_URI, email)))));
  }

  @Test
  public void complete_password_challenge__should_throw__when_missing_data() {
    given()
        .when()
        .post(completePasswordChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Required\",\"challengeId\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void complete_password_challenge__should_throw__when_invalid_data() {
    given()
        .when()
        .formParam("email", "random")
        .formParam("password", "random")
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, "random")
        .post(completePasswordChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"password\":\"Password must be at least 8 characters long\",\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void complete_password_challenge__should_throw__when_random_challenge_id() {
    given()
        .when()
        .formParam("email", "user@gmail.com")
        .formParam("password", "password12345")
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, UUID.randomUUID())
        .post(completePasswordChallengeEndpoint)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void complete_password_challenge__should_throw__when_random_user() {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String challengeId = authorizationFlows().chooseAccount(email);

    given()
        .when()
        .formParam("email", email)
        .formParam("password", UUID.randomUUID())
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, challengeId)
        .post(completePasswordChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void complete_password_challenge__should_throw__when_random_password()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    signUpFlows().signUpAndLogin(email, password);
    String challengeId = authorizationFlows().chooseAccount(email);

    given()
        .when()
        .formParam("email", email)
        .formParam("password", UUID.randomUUID())
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, challengeId)
        .post(completePasswordChallengeEndpoint)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }

  @Test
  public void complete_password_challenge__should_succeed__when_valid_credentials()
      throws JsonProcessingException {
    String email = AuthApiTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    signUpFlows().signUpAndLogin(email, password);

    String challengeId = authorizationFlows().chooseAccount(email);
    pwdAuthorizationFlows().completePwdChallengeSuccess(email, password, challengeId);
  }
}
