package com.rebrowse.auth.utils;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.sso.AbstractIdentityProvider;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import io.restassured.response.ValidatableResponse;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

public final class OAuthFlows extends AbstractTestFlow {

  public OAuthFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
  }

  public ValidatableResponse callbackAuthorization(URI callbackUri) {
    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);
    return given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", UUID.randomUUID())
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(callbackUri)
        .then()
        .statusCode(302)
        .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, "")
        .cookie(SsoSession.COOKIE_NAME);
  }
}
