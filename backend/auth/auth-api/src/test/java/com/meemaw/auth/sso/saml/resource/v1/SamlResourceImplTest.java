package com.meemaw.auth.sso.saml.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.AbstractIdentityProviderService;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.test.setup.RestAssuredUtils;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
public class SamlResourceImplTest {

  @Inject MockMailbox mailbox;
  @Inject ObjectMapper objectMapper;
  @Inject SamlServiceImpl samlService;
  @Inject UserDatasource userDatasource;
  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.CALLBACK_PATH)
  URI callbackUri;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.SIGNIN_PATH)
  URI signInUri;

  @Test
  public void sign_in__should_fail__when_no_dest() {
    given()
        .when()
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"destination\":\"Required\"}}}"));
  }

  @Test
  public void sign_in__should_fail__when_no_referer() {
    given()
        .when()
        .queryParam("dest", "/test")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"referer required\"}}"));
  }

  @Test
  public void sign_in__should_fail__when_malformed_referer() {
    given()
        .header("referer", "malformed")
        .when()
        .queryParam("dest", "/test")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"no protocol: malformed\"}}"));
  }

  @Test
  public void sign_in__should_start_flow__by_redirecting_to_provider() {
    String referer = "http://localhost:3000";
    String dest = "/test";
    String expectedLocationBase =
        "https://snuderls.okta.com/app/snuderlsorg446661_insightdev_1/exkw843tlucjMJ0kL4x6/sso/saml?RelayState=";

    Response response =
        given()
            .header("referer", referer)
            .config(RestAssuredUtils.dontFollowRedirects())
            .when()
            .queryParam("dest", dest)
            .get(signInUri);

    response.then().statusCode(302).header("Location", startsWith(expectedLocationBase));
    String state = response.header("Location").replace(expectedLocationBase, "");
    String destination =
        state.substring(AbstractIdentityProviderService.SECURE_STATE_PREFIX_LENGTH);
    assertEquals(URLEncoder.encode(referer + dest, StandardCharsets.UTF_8), destination);
  }

  @Test
  public void oauth2callback__should_fail__when_no_params() {
    given()
        .when()
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"RelayState\":\"Required\",\"SAMLResponse\":\"Required\"}}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_random_saml_response() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);
    given()
        .when()
        .formParam("SAMLResponse", "random")
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid SAMLResponse\"}}"));
  }

  @Test
  public void oauth2callback__should_fail__on_invalid_state_cookie() {
    String state = URLEncoder.encode("/test", StandardCharsets.UTF_8);

    given()
        .when()
        .formParam("SAMLResponse", "random")
        .formParam("RelayState", state)
        .post(callbackUri)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Invalid state parameter\"}}"));
  }

  @Test
  public void callback__should_sign_up_user__when_valid_saml_response() {
    String email = "matej.snuderl@snuderls.eu";
    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String SAMLResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcKQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQKQWxWVE1STXdFUVlEVlFRSURBcERZV3hwWm05eWJtbGhNUll3RkFZRFZRUUhEQTFUWVc0Z1JuSmhibU5wYzJOdk1RMHdDd1lEVlFRSwpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzAKeEYzR2dSbnVnVllWbDVtQUNkd0tlKzNzTmZ4REFtSHp6YXY3UzFqSzMzSzRxQ3duZW5ZeXhCRXBnWElSY3JQSmo0S1Zza3NlSmVmWQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkKRmM5RHVDS2ozVklZUFZ6dGRRaVpENElrRXVzZzBzZFB1MHZiaEZwY1puUEJSbGtaYzZYT05KbW9CU1NudFhCY1BBL1V0aXFMV0pvcwpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMKM0d6MVBnM1U1cDlIWStrem5nSGxBcDJhajVmUERxU0srWmlqSGdBbE1WUGV1RkdiSUplclRoWEtGUWpZd0E5VmhJWVpMSENocmJ5ZgowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1CnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXEKVTVmaXN3cGU2U3J0c2JWQXZIMVdHOTlqbVRvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6U3RhdHVzIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6QXNzZXJ0aW9uIElEPSJpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTA4MTM3NTIwOTE4OTAyMjMiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+cy9zdFZnckdhSTQwNVFRT3UxQTl0MHNzQ2U5TUh2SXV4YmV1VjdYY054Zz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+VnE0U0NMYitIQm1QTzJ0SlhsVDVFVkRMeEo5WWpRaVNNTWJPMDR3WW1mdUVUa1F0Z1lQSEFQSHNFNkEzOHV2ODN2cENLZ1ROaHRHMUJHQzdPcmJZSVpoSzliQXRRa0E0K3dFREo3N1dYRHIvcnZmaGNhS0dhaEM2UzZ4SHN3V2ZicTArZzZIRFNqOEZURFNiZ2pianQxVVB0QVBzOUxJUERqNWV6d1pISVRnQ0IwYUYxdFI5RDlWTjR6MUcrZTZZNGRyZW1EUFFvMDY0TEtRMnlaNWFucTJIbVMwZHZhQlpBV09CZDFQcjFWRVJzWGhFNkhLKzE0by9vVitpSVd5MWFOZnJIc2YrV3dxRkdqUzZzNCtrOExCL0xFODdiYlFPZjFHSFN6QmQwWW5CUDdVdnZQcjArM1ltSnBXbkZRTkpHWG44ZUZnR1ZTK01QS2ZBdm1ybEV3PT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcKQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQKQWxWVE1STXdFUVlEVlFRSURBcERZV3hwWm05eWJtbGhNUll3RkFZRFZRUUhEQTFUWVc0Z1JuSmhibU5wYzJOdk1RMHdDd1lEVlFRSwpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzAKeEYzR2dSbnVnVllWbDVtQUNkd0tlKzNzTmZ4REFtSHp6YXY3UzFqSzMzSzRxQ3duZW5ZeXhCRXBnWElSY3JQSmo0S1Zza3NlSmVmWQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkKRmM5RHVDS2ozVklZUFZ6dGRRaVpENElrRXVzZzBzZFB1MHZiaEZwY1puUEJSbGtaYzZYT05KbW9CU1NudFhCY1BBL1V0aXFMV0pvcwpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMKM0d6MVBnM1U1cDlIWStrem5nSGxBcDJhajVmUERxU0srWmlqSGdBbE1WUGV1RkdiSUplclRoWEtGUWpZd0E5VmhJWVpMSENocmJ5ZgowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1CnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXEKVTVmaXN3cGU2U3J0c2JWQXZIMVdHOTlqbVRvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMjpTdWJqZWN0IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6TmFtZUlEIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6dW5zcGVjaWZpZWQiPm1hdGVqLnNudWRlcmxAc251ZGVybHMuZXU8L3NhbWwyOk5hbWVJRD48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDZUMTg6NTU6NDguNDEzWiIgUmVjaXBpZW50PSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siLz48L3NhbWwyOlN1YmplY3RDb25maXJtYXRpb24+PC9zYW1sMjpTdWJqZWN0PjxzYW1sMjpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMC0wOS0wNlQxODo0NTo0OC40MTNaIiBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDZUMTg6NTU6NDguNDEzWiIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjxzYW1sMjpBdWRpZW5jZT5pbnNpZ2h0LWRldjwvc2FtbDI6QXVkaWVuY2U+PC9zYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjwvc2FtbDI6Q29uZGl0aW9ucz48c2FtbDI6QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDIwLTA5LTA2VDE1OjAwOjE5LjM5OVoiIFNlc3Npb25JbmRleD0iaWQxNTk5NDE4MjQ4NDExLjEyODk4MDg3NDEiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXV0aG5Db250ZXh0PjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMjpBdXRobkNvbnRleHQ+PC9zYW1sMjpBdXRoblN0YXRlbWVudD48c2FtbDI6QXR0cmlidXRlU3RhdGVtZW50IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXR0cmlidXRlIE5hbWU9ImdpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1bnNwZWNpZmllZCI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+TWF0ZWo8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iZmFtaWx5TmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1bnNwZWNpZmllZCI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+U251ZGVybDwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PC9zYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PC9zYW1sMjpBc3NlcnRpb24+PC9zYW1sMnA6UmVzcG9uc2U+";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", SAMLResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(302)
        .header("Location", Location)
        .cookie(SsoSession.COOKIE_NAME);

    AuthUser user = userDatasource.findUser(email).toCompletableFuture().join().get();
    assertEquals(user.getFullName(), "Matej Snuderl");
  }
}
