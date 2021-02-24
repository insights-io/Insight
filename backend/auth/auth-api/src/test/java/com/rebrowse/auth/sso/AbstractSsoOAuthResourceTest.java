package com.rebrowse.auth.sso;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.api.RebrowseApi;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.model.auth.SsoMethod;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import com.rebrowse.model.user.UserRole;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

public abstract class AbstractSsoOAuthResourceTest extends AbstractSsoResourceTest {

  public abstract URI signInUri();

  public abstract URI callbackUri();

  public abstract AbstractIdentityProvider service();

  public abstract AbstractOAuthClient<?, ?, ?> installMockForClient(String email);

  @Test
  public void sign_in__should_fail__when_missing_redirect() {
    given()
        .when()
        .get(signInUri())
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\"}}}"));
  }

  @Test
  public void sign_in__should_fail__when_malformed_redirect() {
    given()
        .when()
        .queryParam("redirect", "random")
        .get(signInUri())
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Resource Not Found\"}}"));
  }

  @Test
  public void sign_in__should_fail__when_malformed_email() {
    given()
        .when()
        .queryParam("redirect", GlobalTestData.LOCALHOST_REDIRECT)
        .queryParam("email", UUID.randomUUID())
        .get(signInUri())
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void oauth2callback__should_fail__when_no_params() {
    given()
        .when()
        .get(callbackUri())
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"code\":\"Required\",\"state\":\"Required\"}}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_too_short_state_parameter() {
    String state = URLEncoder.encode("test", RebrowseApi.CHARSET);
    given()
        .when()
        .queryParam("code", UUID.randomUUID())
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(callbackUri())
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_state_cookie_miss_match() {
    String state =
        AbstractIdentityProvider.secureState(
            URLEncoder.encode(GlobalTestData.LOCALHOST_REDIRECT, RebrowseApi.CHARSET));

    given()
        .when()
        .queryParam("code", UUID.randomUUID())
        .queryParam("state", state)
        .get(callbackUri())
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void oauth2callback__should_work__when_sso_organization_no_open_membership()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String domain =
        EmailUtils.getDomain(authorizationFlows().retrieveUserData(sessionId).getUser().getEmail());

    ssoSetupFlows().create(SsoMethod.fromString(service().getMethod().getKey()), sessionId);

    String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
    String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);

    installMockForClient(newUserEmail);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", UUID.randomUUID())
        .queryParam("state", state)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
        .get(callbackUri())
        .then()
        .statusCode(302)
        .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
        .cookie(SsoSession.COOKIE_NAME)
        .cookie(SsoAuthorizationSession.COOKIE_NAME, "");
  }

  @Test
  public void oauth2callback__should_register_user_with_organization_default_role__when_sso_signup()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String domain =
        EmailUtils.getDomain(authorizationFlows().retrieveUserData(sessionId).getUser().getEmail());

    ssoSetupFlows().create(SsoMethod.fromString(service().getMethod().getKey()), sessionId);

    for (UserRole userRole : UserRole.values()) {
      Organization organization =
          Organization.update(
                  OrganizationUpdateParams.builder()
                      .defaultRole(userRole)
                      .openMembership(true)
                      .build(),
                  sdkRequest().sessionId(sessionId).build())
              .toCompletableFuture()
              .join();

      String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
      String state = AbstractIdentityProvider.secureState(GlobalTestData.LOCALHOST_REDIRECT);

      installMockForClient(newUserEmail);

      String createdUserSessionId =
          given()
              .when()
              .config(RestAssuredUtils.dontFollowRedirects())
              .queryParam("code", UUID.randomUUID())
              .queryParam("state", state)
              .cookie(SsoAuthorizationSession.COOKIE_NAME, state)
              .get(callbackUri())
              .then()
              .statusCode(302)
              .header(HttpHeaders.LOCATION, GlobalTestData.LOCALHOST_REDIRECT)
              .cookie(SsoSession.COOKIE_NAME)
              .cookie(SsoAuthorizationSession.COOKIE_NAME, "")
              .extract()
              .detailedCookie(SsoSession.COOKIE_NAME)
              .getValue();

      UserData userData = authorizationFlows().retrieveUserData(createdUserSessionId);

      assertEquals(domain, EmailUtils.getDomain(userData.getUser().getEmail()));
      assertEquals(userData.getOrganization(), organization);
      assertEquals(userRole, userData.getOrganization().getDefaultRole());
    }
  }
}
