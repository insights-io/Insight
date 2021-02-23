package com.rebrowse.test.utils.auth;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.rebrowse.auth.accounts.model.ChooseAccountAction;
import com.rebrowse.auth.accounts.model.ChooseAccountSsoRedirectResponseDTO;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.accounts.resource.v1.AccountsResource;
import com.rebrowse.auth.password.resource.v1.AuthorizationPwdChallengeResource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.model.auth.ApiKey;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.test.matchers.SameJSON;
import io.restassured.common.mapper.TypeRef;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class AuthorizationFlows extends AbstractTestFlow {

  private final URI chooseAccountEndpoint;
  private final URI completePwdChallengeEndpoint;

  public AuthorizationFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
    this.chooseAccountEndpoint =
        UriBuilder.fromUri(baseUri).path(AccountsResource.PATH + "/choose").build();
    this.completePwdChallengeEndpoint =
        UriBuilder.fromUri(baseUri).path(AuthorizationPwdChallengeResource.PATH).build();
  }

  public String chooseAccount(String email) {
    return given()
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .formParam("email", email)
        .post(chooseAccountEndpoint)
        .then()
        .statusCode(200)
        .body(SameJSON.sameJson("{\"data\":{\"action\":\"PWD_CHALLENGE\"}}"))
        .extract()
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME);
  }

  public String login(String email, String password) {
    String pwdChallengeId = chooseAccount(email);
    return given()
        .when()
        .formParam("email", email)
        .formParam("password", password)
        .cookie(AuthorizationPwdChallengeSession.COOKIE_NAME, pwdChallengeId)
        .post(completePwdChallengeEndpoint)
        .then()
        .statusCode(200)
        .body(
            SameJSON.sameJson(
                "{\"data\":{\"location\":\"http://localhost:3000/test\",\"action\":\"SUCCESS\"}}"))
        .extract()
        .detailedCookie(SsoSession.COOKIE_NAME)
        .getValue();
  }

  public String loginWithAdminUser() {
    return login(GlobalTestData.REBROWSE_ADMIN_EMAIL, GlobalTestData.REBROWSE_ADMIN_PASSWORD);
  }

  public ApiKey createApiKey(String sessionId) {
    return ApiKey.create(sdkRequest().sessionId(sessionId).build()).toCompletableFuture().join();
  }

  /* SSO */
  public ChooseAccountSsoRedirectResponseDTO chooseAccountRedirectToSso(
      String email, String authorizationUriMatcher) {
    DataResponse<ChooseAccountSsoRedirectResponseDTO> dataResponse =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
            .formParam("email", email)
            .post(chooseAccountEndpoint)
            .then()
            .statusCode(200)
            .cookie(SsoAuthorizationSession.COOKIE_NAME)
            .extract()
            .as(new TypeRef<>() {});

    ChooseAccountSsoRedirectResponseDTO data = dataResponse.getData();

    assertEquals(data.getAction(), ChooseAccountAction.SSO_REDIRECT);
    assertThat(data.getLocation().toString(), matchesPattern(authorizationUriMatcher));

    return data;
  }

  public UserData retrieveUserData(String sessionId) {
    return UserData.retrieve(sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();
  }
}
