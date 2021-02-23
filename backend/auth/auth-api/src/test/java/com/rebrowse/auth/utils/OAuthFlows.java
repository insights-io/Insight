package com.rebrowse.auth.utils;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import java.net.URI;
import java.util.UUID;

public final class OAuthFlows extends AbstractTestFlow {

  public OAuthFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
  }

  public void totpMfaChallenge(URI callbackUri) {
    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    ValidatableResponse response =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .queryParam("code", UUID.randomUUID())
            .queryParam("state", state)
            .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
            .get(callbackUri)
            .then();

    DataResponse<AuthorizationMfaChallengeResponseDTO> dataResponse =
        response.extract().as(new TypeRef<>() {});
    String challengeId = dataResponse.getData().getChallengeId();

    response
        .body(
            sameJson(
                String.format(
                    "{\"data\":{\"action\":\"MFA_CHALLENGE\",\"challengeId\":\"%s\",\"methods\":[\"totp\"]}}",
                    challengeId)))
        .cookie(AuthorizationMfaChallengeSession.COOKIE_NAME, challengeId)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, "");
  }
}
