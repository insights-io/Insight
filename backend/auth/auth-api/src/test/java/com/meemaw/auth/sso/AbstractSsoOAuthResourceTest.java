package com.meemaw.auth.sso;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuthClient;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.test.setup.RestAssuredUtils;
import com.rebrowse.model.auth.SessionInfo;
import com.rebrowse.model.auth.SsoMethod;
import com.rebrowse.model.auth.SsoSetup;
import com.rebrowse.model.auth.SsoSetupCreateParams;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import com.rebrowse.model.user.UserRole;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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
        .queryParam("redirect", "http://localhost:3000")
        .queryParam("email", "random")
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
    String state = URLEncoder.encode("test", StandardCharsets.UTF_8);
    given()
        .when()
        .queryParam("code", "random")
        .queryParam("state", state)
        .cookie("state", state)
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
            URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

    given()
        .when()
        .queryParam("code", "04fc2d3f11120e6ca0e2")
        .queryParam("state", state)
        .get(callbackUri())
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void oauth2callback__should_throw__when_sso_organization_no_open_membership()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomBusinessCredentials();
    String domain =
        EmailUtils.domainFromEmail(authApi().getSessionInfo(sessionId).getUser().getEmail());

    SsoSetup.create(
            SsoSetupCreateParams.builder()
                .method(SsoMethod.fromString(service().getLoginMethod().getKey()))
                .build(),
            authApi().sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
    String Location = "https://www.insight.io/my_path";
    String state = AbstractIdentityProvider.secureState(Location);

    installMockForClient(newUserEmail);

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .queryParam("code", "any")
        .queryParam("state", state)
        .cookie("state", state)
        .get(callbackUri())
        .then()
        .statusCode(302)
        .header(
            "Location",
            "https://www.insight.io/my_path?oauthError=Organization+does+not+support+open+membership.+Please+contact+your+Administrator");
  }

  @Test
  public void oauth2callback__should_register_user_with_organization_default_role__when_sso_signup()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomBusinessCredentials();
    String domain =
        EmailUtils.domainFromEmail(authApi().getSessionInfo(sessionId).getUser().getEmail());

    SsoSetup.create(
            SsoSetupCreateParams.builder()
                .method(SsoMethod.fromString(service().getLoginMethod().getKey()))
                .build(),
            authApi().sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();

    for (UserRole userRole : UserRole.values()) {
      Organization organization =
          Organization.update(
                  OrganizationUpdateParams.builder()
                      .defaultRole(userRole)
                      .openMembership(true)
                      .build(),
                  authApi().sdkRequest().sessionId(sessionId).build())
              .toCompletableFuture()
              .join();

      String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
      String Location = "https://www.insight.io/my_path";
      String state = AbstractIdentityProvider.secureState(Location);

      installMockForClient(newUserEmail);

      String createdUserSessionId =
          given()
              .when()
              .config(RestAssuredUtils.dontFollowRedirects())
              .queryParam("code", "any")
              .queryParam("state", state)
              .cookie("state", state)
              .get(callbackUri())
              .then()
              .statusCode(302)
              .header("Location", Location)
              .cookie(SsoSession.COOKIE_NAME)
              .extract()
              .detailedCookie(SsoSession.COOKIE_NAME)
              .getValue();

      SessionInfo sessionInfo = authApi().getSessionInfo(createdUserSessionId);

      assertEquals(domain, EmailUtils.domainFromEmail(sessionInfo.getUser().getEmail()));
      assertEquals(sessionInfo.getOrganization(), organization);
      assertEquals(userRole, sessionInfo.getOrganization().getDefaultRole());
    }
  }
}
