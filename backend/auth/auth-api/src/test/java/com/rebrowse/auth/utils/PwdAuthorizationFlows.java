package com.rebrowse.auth.utils;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import com.rebrowse.test.utils.auth.AuthorizationFlows;
import com.rebrowse.auth.accounts.model.AuthorizationSuccessResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.password.resource.v1.AuthorizationPwdChallengeResource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public final class PwdAuthorizationFlows extends AbstractTestFlow {

  private final URI completePwdChallengeEndpoint;

  public PwdAuthorizationFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
    this.completePwdChallengeEndpoint =
        UriBuilder.fromUri(baseUri).path(AuthorizationPwdChallengeResource.PATH).build();
  }

  private Response completePwdChallengeBase(String email, String password, String challengeId) {
    return given()
        .when()
        .formParam("email", email)
        .formParam("password", password)
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, challengeId)
        .post(completePwdChallengeEndpoint);
  }

  public ValidatableResponse completePwdChallenge(
      String email, String password, String challengeId) {
    return completePwdChallengeBase(email, password, challengeId).then().statusCode(200);
  }

  public AuthorizationSuccessResponseDTO completePwdChallengeSuccess(
      String email, String password, String challengeId) throws JsonProcessingException {
    DataResponse<AuthorizationSuccessResponseDTO> dataResponse =
        completePwdChallengeBase(email, password, challengeId)
            .then()
            .statusCode(200)
            .cookie(SsoSession.COOKIE_NAME)
            .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, "")
            .body(
                sameJson(
                    objectMapper.writeValueAsString(
                        AuthApiTestData.LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE)))
            .extract()
            .as(new TypeRef<>() {});

    return dataResponse.getData();
  }

  public void completePwdChallengeInvalidCredentials(String email, String password) {
    String challengeId = new AuthorizationFlows(baseUri, objectMapper).chooseAccount(email);
    completePwdChallengeInvalidCredentials(email, password, challengeId);
  }

  public void completePwdChallengeInvalidCredentials(
      String email, String password, String challengeId) {
    completePwdChallengeBase(email, password, challengeId)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid email or password\"}}"));
  }
}
