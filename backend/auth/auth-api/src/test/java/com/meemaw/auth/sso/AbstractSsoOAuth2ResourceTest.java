package com.meemaw.auth.sso;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.resource.v1.OrganizationResource;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Client;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.SessionInfoDTO;
import com.meemaw.test.setup.RestAssuredUtils;
import io.restassured.http.ContentType;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

public abstract class AbstractSsoOAuth2ResourceTest extends AbstractSsoResourceTest {

  public abstract URI signInUri();

  public abstract URI callbackUri();

  public abstract AbstractIdpService service();

  public abstract AbstractOAuth2Client<?, ?, ?> installMockForClient(String email);

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
        AbstractIdpService.secureState(URLEncoder.encode(SIMPLE_REDIRECT, StandardCharsets.UTF_8));

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
        EmailUtils.domainFromEmail(authApi().getSessionInfo(sessionId).get().getUser().getEmail());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new CreateSsoSetupDTO(
                    SsoMethod.fromString(service().getLoginMethod().getKey()), null)))
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
    String Location = "https://www.insight.io/my_path";
    String state = AbstractIdpService.secureState(Location);

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
        EmailUtils.domainFromEmail(authApi().getSessionInfo(sessionId).get().getUser().getEmail());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new CreateSsoSetupDTO(
                    SsoMethod.fromString(service().getLoginMethod().getKey()), null)))
        .post(SsoSetupResource.PATH)
        .then()
        .statusCode(201);

    for (UserRole userRole : UserRole.values()) {
      given()
          .when()
          .contentType(ContentType.JSON)
          .cookie(SsoSession.COOKIE_NAME, sessionId)
          .body(
              objectMapper.writeValueAsString(
                  Map.of("defaultRole", userRole.getKey(), "openMembership", true)))
          .patch(OrganizationResource.PATH)
          .then()
          .statusCode(200);

      Organization organization = authApi().getOrganization(sessionId).get();

      String newUserEmail = String.format("%s@%s", UUID.randomUUID(), domain);
      String Location = "https://www.insight.io/my_path";
      String state = AbstractIdpService.secureState(Location);

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

      SessionInfoDTO sessionInfo = authApi().getSessionInfo(createdUserSessionId).get();

      assertEquals(domain, EmailUtils.domainFromEmail(sessionInfo.getUser().getEmail()));
      assertEquals(sessionInfo.getOrganization(), organization);
      assertEquals(userRole, sessionInfo.getOrganization().getDefaultRole());
    }
  }
}
