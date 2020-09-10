package com.meemaw.auth.sso.saml.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SamlResourceImplTest {

  @Inject SamlServiceImpl samlService;
  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;
  @Inject SsoSetupDatasource ssoSetupDatasource;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.CALLBACK_PATH)
  URI callbackUri;

  @TestHTTPResource(SamlResource.PATH + "/" + SamlResource.SIGNIN_PATH)
  URI signInUri;

  @Test
  public void sign_in__should_fail__when_no_params() {
    given()
        .when()
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"redirect\":\"Required\",\"email\":\"Required\"}}}"));
  }

  @Test
  public void sign_in__should_fail__when_malformed_email() {
    given()
        .header("referer", "malformed")
        .when()
        .queryParam("redirect", "/test")
        .queryParam("email", "matej.snuderl")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"email\":\"must be a well-formed email address\"}}}"));
  }

  @Test
  public void sign_in__should_fail__when_no_referer() {
    given()
        .when()
        .queryParam("redirect", "/test")
        .queryParam("email", "matej.snuderl@snuderls.eu")
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
        .queryParam("redirect", "/test")
        .queryParam("email", "matej.snuderl@snuderls.eu")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"no protocol: malformed\"}}"));
  }

  @Test
  public void sign_in__should_fail__when_domain_with_no_sso_setup() {
    String referer = "http://localhost:3000";
    String redirect = "/test";

    given()
        .when()
        .header("referer", referer)
        .queryParam("redirect", redirect)
        .queryParam("email", "matej.snuderl@snuderls.iooooo")
        .get(signInUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"That email or domain isn’t registered for SSO.\"}}"));
  }

  @Test
  public void sign_in__should_redirect_to_sso_provider__when_sso_setup()
      throws MalformedURLException {
    String referer = "http://localhost:3000";
    String redirect = "/test";
    String email = "matej.snuderl@snuderls.mo";

    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    URL configurationEndpoint =
        new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata");

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .header("referer", referer)
        .queryParam("redirect", redirect)
        .queryParam("email", email)
        .get(signInUri)
        .then()
        .statusCode(302)
        .header(
            "Location",
            Matchers.matchesPattern(
                "^https:\\/\\/snuderls\\.okta\\.com\\/app\\/snuderlsorg446661_insightdev_1\\/exkw843tlucjMJ0kL4x6\\/sso\\/saml\\?RelayState=(.*)http%3A%2F%2Flocalhost%3A3000%2Ftest$"))
        .cookie(SsoSignInSession.COOKIE_NAME);
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
  public void callback__should_fail__when_configuration_endpoint_is_down()
      throws MalformedURLException {
    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    String email = "matej.snuderl@snuderls.test";
    URL configurationEndpoint = new URL("http://localhost:1000");
    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String samlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcNCkExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekVOTUFzR0ExVUVDZ3dFVDJ0MFlURVUNCk1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFVEFQQmdOVkJBTU1DSE51ZFdSbGNteHpNUnd3R2dZSktvWklodmNOQVFrQkZnMXANCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQNCkFsVlRNUk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExVFlXNGdSbkpoYm1OcGMyTnZNUTB3Q3dZRFZRUUsNCkRBUlBhM1JoTVJRd0VnWURWUVFMREF0VFUwOVFjbTkyYVdSbGNqRVJNQThHQTFVRUF3d0ljMjUxWkdWeWJITXhIREFhQmdrcWhraUcNCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzANCnhGM0dnUm51Z1ZZVmw1bUFDZHdLZSszc05meERBbUh6emF2N1MxakszM0s0cUN3bmVuWXl4QkVwZ1hJUmNyUEpqNEtWc2tzZUplZlkNCnYrOGpXdTJ0cWFlY0tQRUJDd1ZxSC9NcnI3Qld6QW9jTDlHaUx5NjQvY2RSZ2dQVkRNOGxGV1pkdXYvajJlMDVNemQ5a2xFdUtOaWENCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkNCkZjOUR1Q0tqM1ZJWVBWenRkUWlaRDRJa0V1c2cwc2RQdTB2YmhGcGNablBCUmxrWmM2WE9OSm1vQlNTbnRYQmNQQS9VdGlxTFdKb3MNCkE0R095Q3RFR2tQcE5uYi9CdUJWUzBZa0hWeUgrM1Z2QWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBR2dML3pMb29RaCsNCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMNCjNHejFQZzNVNXA5SFkra3puZ0hsQXAyYWo1ZlBEcVNLK1ppakhnQWxNVlBldUZHYklKZXJUaFhLRlFqWXdBOVZoSVlaTEhDaHJieWYNCjB2OFdnc0JEZFE3aWJzcW1XaTcwRlBmb0hYU0tpWWZUMUxlTE1XcWVlSUJtMzJtMUFCWDNDQXcxZ1lHZlVBWlhINVBYQS80OEwzUXUNCnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXENClU1Zmlzd3BlNlNydHNiVkF2SDFXRzk5am1Ubz08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDJwOlN0YXR1cyB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwycDpTdGF0dXNDb2RlIFZhbHVlPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6c3RhdHVzOlN1Y2Nlc3MiLz48L3NhbWwycDpTdGF0dXM+PHNhbWwyOkFzc2VydGlvbiBJRD0iaWQ1MzI1NjIzNTM1MDgxMzc1MjA5MTg5MDIyMyIgSXNzdWVJbnN0YW50PSIyMDIwLTA5LTA2VDE4OjUwOjQ4LjQxM1oiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly93d3cub2t0YS5jb20vZXhrdzg0M3RsdWNqTUowa0w0eDY8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+PGRzOlJlZmVyZW5jZSBVUkk9IiNpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyBQcmVmaXhMaXN0PSJ4cyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPnMvc3RWZ3JHYUk0MDVRUU91MUE5dDBzc0NlOU1Idkl1eGJldVY3WGNOeGc9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPlZxNFNDTGIrSEJtUE8ydEpYbFQ1RVZETHhKOVlqUWlTTU1iTzA0d1ltZnVFVGtRdGdZUEhBUEhzRTZBMzh1djgzdnBDS2dUTmh0RzFCR0M3T3JiWUlaaEs5YkF0UWtBNCt3RURKNzdXWERyL3J2ZmhjYUtHYWhDNlM2eEhzd1dmYnEwK2c2SERTajhGVERTYmdqYmp0MVVQdEFQczlMSVBEajVlendaSElUZ0NCMGFGMXRSOUQ5Vk40ejFHK2U2WTRkcmVtRFBRbzA2NExLUTJ5WjVhbnEySG1TMGR2YUJaQVdPQmQxUHIxVkVSc1hoRTZISysxNG8vb1YraUlXeTFhTmZySHNmK1d3cUZHalM2czQrazhMQi9MRTg3YmJRT2YxR0hTekJkMFluQlA3VXZ2UHIwKzNZbUpwV25GUU5KR1huOGVGZ0dWUytNUEtmQXZtcmxFdz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEb0RDQ0FvaWdBd0lCQWdJR0FYUmo3SWUvTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdRTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHDQpBMVVFQ0F3S1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ3d05VMkZ1SUVaeVlXNWphWE5qYnpFTk1Bc0dBMVVFQ2d3RVQydDBZVEVVDQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwDQpibVp2UUc5cmRHRXVZMjl0TUI0WERUSXdNRGt3TmpFME5UWTFPVm9YRFRNd01Ea3dOakUwTlRjMU9Wb3dnWkF4Q3pBSkJnTlZCQVlUDQpBbFZUTVJNd0VRWURWUVFJREFwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSERBMVRZVzRnUm5KaGJtTnBjMk52TVEwd0N3WURWUVFLDQpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHDQo5dzBCQ1FFV0RXbHVabTlBYjJ0MFlTNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1BEdy8wDQp4RjNHZ1JudWdWWVZsNW1BQ2R3S2UrM3NOZnhEQW1IenphdjdTMWpLMzNLNHFDd25lbll5eEJFcGdYSVJjclBKajRLVnNrc2VKZWZZDQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhDQpEdHEzaWxiMGFFaXhtUE5lNElRaTJTT2Zna0Z1SlU3QVBuakNaSkJxVEtGam9mYkdtVEdXeVhxdzdONVZoWThGQm03SHAvWXYwdmJZDQpGYzlEdUNLajNWSVlQVnp0ZFFpWkQ0SWtFdXNnMHNkUHUwdmJoRnBjWm5QQlJsa1pjNlhPTkptb0JTU250WEJjUEEvVXRpcUxXSm9zDQpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrDQpqbEQwRHpsTGRRT2VmMjVnVHhkQ0gvcEc5QTV2ZG9WQ1NkbUM4Z2FKS3A5SHhZVm1zOHpBZ1RmU3BCZ1I5UnpDekJIK2NBWEdTeVRjDQozR3oxUGczVTVwOUhZK2t6bmdIbEFwMmFqNWZQRHFTSytaaWpIZ0FsTVZQZXVGR2JJSmVyVGhYS0ZRall3QTlWaElZWkxIQ2hyYnlmDQowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1DQpzWnY0d01DN0tDRnZzcmpJUlcrQVNWcWdJNlpOb1VWUlhBUlBEQi9GZ2tOaTZGdko4Z0ttUkU4NTBleGk0NisveVJ0b2xQUDM3VkFxDQpVNWZpc3dwZTZTcnRzYlZBdkgxV0c5OWptVG89PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwyOlN1YmplY3QgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+bWF0ZWouc251ZGVybEBzbnVkZXJscy50ZXN0PC9zYW1sMjpOYW1lSUQ+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbkRhdGEgTm90T25PckFmdGVyPSIyMDIwLTA5LTA2VDE4OjU1OjQ4LjQxM1oiIFJlY2lwaWVudD0iaHR0cDovL2xvY2FsaG9zdDo4MDgwL3YxL3Nzby9zYW1sL2NhbGxiYWNrIi8+PC9zYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uPjwvc2FtbDI6U3ViamVjdD48c2FtbDI6Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMjAtMDktMDZUMTg6NDU6NDguNDEzWiIgTm90T25PckFmdGVyPSIyMDIwLTA5LTA2VDE4OjU1OjQ4LjQxM1oiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48c2FtbDI6QXVkaWVuY2U+aW5zaWdodC1kZXY8L3NhbWwyOkF1ZGllbmNlPjwvc2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48L3NhbWwyOkNvbmRpdGlvbnM+PHNhbWwyOkF1dGhuU3RhdGVtZW50IEF1dGhuSW5zdGFudD0iMjAyMC0wOS0wNlQxNTowMDoxOS4zOTlaIiBTZXNzaW9uSW5kZXg9ImlkMTU5OTQxODI0ODQxMS4xMjg5ODA4NzQxIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOkF1dGhuQ29udGV4dD48c2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L3NhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPjwvc2FtbDI6QXV0aG5Db250ZXh0Pjwvc2FtbDI6QXV0aG5TdGF0ZW1lbnQ+PHNhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudCB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJnaXZlbk5hbWUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dW5zcGVjaWZpZWQiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPk1hdGVqPC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48c2FtbDI6QXR0cmlidXRlIE5hbWU9ImZhbWlseU5hbWUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dW5zcGVjaWZpZWQiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPlNudWRlcmw8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjwvc2FtbDI6QXR0cmlidXRlU3RhdGVtZW50Pjwvc2FtbDI6QXNzZXJ0aW9uPjwvc2FtbDJwOlJlc3BvbnNlPg==";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Connection refused\"}}}"));
  }

  @Test
  public void callback__should_fail__when_configuration_endpoint_serves_broken_xml()
      throws MalformedURLException {
    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    String email = "matej.snuderl@snuderls.test2";
    URL configurationEndpoint = new URL("https://google.com");
    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String samlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcNCkExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekVOTUFzR0ExVUVDZ3dFVDJ0MFlURVUNCk1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFVEFQQmdOVkJBTU1DSE51ZFdSbGNteHpNUnd3R2dZSktvWklodmNOQVFrQkZnMXANCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQNCkFsVlRNUk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExVFlXNGdSbkpoYm1OcGMyTnZNUTB3Q3dZRFZRUUsNCkRBUlBhM1JoTVJRd0VnWURWUVFMREF0VFUwOVFjbTkyYVdSbGNqRVJNQThHQTFVRUF3d0ljMjUxWkdWeWJITXhIREFhQmdrcWhraUcNCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzANCnhGM0dnUm51Z1ZZVmw1bUFDZHdLZSszc05meERBbUh6emF2N1MxakszM0s0cUN3bmVuWXl4QkVwZ1hJUmNyUEpqNEtWc2tzZUplZlkNCnYrOGpXdTJ0cWFlY0tQRUJDd1ZxSC9NcnI3Qld6QW9jTDlHaUx5NjQvY2RSZ2dQVkRNOGxGV1pkdXYvajJlMDVNemQ5a2xFdUtOaWENCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkNCkZjOUR1Q0tqM1ZJWVBWenRkUWlaRDRJa0V1c2cwc2RQdTB2YmhGcGNablBCUmxrWmM2WE9OSm1vQlNTbnRYQmNQQS9VdGlxTFdKb3MNCkE0R095Q3RFR2tQcE5uYi9CdUJWUzBZa0hWeUgrM1Z2QWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBR2dML3pMb29RaCsNCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMNCjNHejFQZzNVNXA5SFkra3puZ0hsQXAyYWo1ZlBEcVNLK1ppakhnQWxNVlBldUZHYklKZXJUaFhLRlFqWXdBOVZoSVlaTEhDaHJieWYNCjB2OFdnc0JEZFE3aWJzcW1XaTcwRlBmb0hYU0tpWWZUMUxlTE1XcWVlSUJtMzJtMUFCWDNDQXcxZ1lHZlVBWlhINVBYQS80OEwzUXUNCnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXENClU1Zmlzd3BlNlNydHNiVkF2SDFXRzk5am1Ubz08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDJwOlN0YXR1cyB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwycDpTdGF0dXNDb2RlIFZhbHVlPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6c3RhdHVzOlN1Y2Nlc3MiLz48L3NhbWwycDpTdGF0dXM+PHNhbWwyOkFzc2VydGlvbiBJRD0iaWQ1MzI1NjIzNTM1MDgxMzc1MjA5MTg5MDIyMyIgSXNzdWVJbnN0YW50PSIyMDIwLTA5LTA2VDE4OjUwOjQ4LjQxM1oiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly93d3cub2t0YS5jb20vZXhrdzg0M3RsdWNqTUowa0w0eDY8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+PGRzOlJlZmVyZW5jZSBVUkk9IiNpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyBQcmVmaXhMaXN0PSJ4cyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPnMvc3RWZ3JHYUk0MDVRUU91MUE5dDBzc0NlOU1Idkl1eGJldVY3WGNOeGc9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPlZxNFNDTGIrSEJtUE8ydEpYbFQ1RVZETHhKOVlqUWlTTU1iTzA0d1ltZnVFVGtRdGdZUEhBUEhzRTZBMzh1djgzdnBDS2dUTmh0RzFCR0M3T3JiWUlaaEs5YkF0UWtBNCt3RURKNzdXWERyL3J2ZmhjYUtHYWhDNlM2eEhzd1dmYnEwK2c2SERTajhGVERTYmdqYmp0MVVQdEFQczlMSVBEajVlendaSElUZ0NCMGFGMXRSOUQ5Vk40ejFHK2U2WTRkcmVtRFBRbzA2NExLUTJ5WjVhbnEySG1TMGR2YUJaQVdPQmQxUHIxVkVSc1hoRTZISysxNG8vb1YraUlXeTFhTmZySHNmK1d3cUZHalM2czQrazhMQi9MRTg3YmJRT2YxR0hTekJkMFluQlA3VXZ2UHIwKzNZbUpwV25GUU5KR1huOGVGZ0dWUytNUEtmQXZtcmxFdz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEb0RDQ0FvaWdBd0lCQWdJR0FYUmo3SWUvTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdRTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHDQpBMVVFQ0F3S1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ3d05VMkZ1SUVaeVlXNWphWE5qYnpFTk1Bc0dBMVVFQ2d3RVQydDBZVEVVDQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwDQpibVp2UUc5cmRHRXVZMjl0TUI0WERUSXdNRGt3TmpFME5UWTFPVm9YRFRNd01Ea3dOakUwTlRjMU9Wb3dnWkF4Q3pBSkJnTlZCQVlUDQpBbFZUTVJNd0VRWURWUVFJREFwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSERBMVRZVzRnUm5KaGJtTnBjMk52TVEwd0N3WURWUVFLDQpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHDQo5dzBCQ1FFV0RXbHVabTlBYjJ0MFlTNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1BEdy8wDQp4RjNHZ1JudWdWWVZsNW1BQ2R3S2UrM3NOZnhEQW1IenphdjdTMWpLMzNLNHFDd25lbll5eEJFcGdYSVJjclBKajRLVnNrc2VKZWZZDQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhDQpEdHEzaWxiMGFFaXhtUE5lNElRaTJTT2Zna0Z1SlU3QVBuakNaSkJxVEtGam9mYkdtVEdXeVhxdzdONVZoWThGQm03SHAvWXYwdmJZDQpGYzlEdUNLajNWSVlQVnp0ZFFpWkQ0SWtFdXNnMHNkUHUwdmJoRnBjWm5QQlJsa1pjNlhPTkptb0JTU250WEJjUEEvVXRpcUxXSm9zDQpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrDQpqbEQwRHpsTGRRT2VmMjVnVHhkQ0gvcEc5QTV2ZG9WQ1NkbUM4Z2FKS3A5SHhZVm1zOHpBZ1RmU3BCZ1I5UnpDekJIK2NBWEdTeVRjDQozR3oxUGczVTVwOUhZK2t6bmdIbEFwMmFqNWZQRHFTSytaaWpIZ0FsTVZQZXVGR2JJSmVyVGhYS0ZRall3QTlWaElZWkxIQ2hyYnlmDQowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1DQpzWnY0d01DN0tDRnZzcmpJUlcrQVNWcWdJNlpOb1VWUlhBUlBEQi9GZ2tOaTZGdko4Z0ttUkU4NTBleGk0NisveVJ0b2xQUDM3VkFxDQpVNWZpc3dwZTZTcnRzYlZBdkgxV0c5OWptVG89PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwyOlN1YmplY3QgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+bWF0ZWouc251ZGVybEBzbnVkZXJscy50ZXN0Mjwvc2FtbDI6TmFtZUlEPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiBSZWNpcGllbnQ9Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC92MS9zc28vc2FtbC9jYWxsYmFjayIvPjwvc2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWwyOlN1YmplY3Q+PHNhbWwyOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDIwLTA5LTA2VDE4OjQ1OjQ4LjQxM1oiIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PHNhbWwyOkF1ZGllbmNlPmluc2lnaHQtZGV2PC9zYW1sMjpBdWRpZW5jZT48L3NhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PC9zYW1sMjpDb25kaXRpb25zPjxzYW1sMjpBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMjAtMDktMDZUMTU6MDA6MTkuMzk5WiIgU2Vzc2lvbkluZGV4PSJpZDE1OTk0MTgyNDg0MTEuMTI4OTgwODc0MSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdXRobkNvbnRleHQ+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPnVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0PC9zYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWwyOkF1dGhuQ29udGV4dD48L3NhbWwyOkF1dGhuU3RhdGVtZW50PjxzYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iZ2l2ZW5OYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5NYXRlajwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJmYW1pbHlOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5TbnVkZXJsPC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4=";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Unable to parse inputstream, it contained invalid XML\"}}}"));
  }

  @Test
  public void callback__should_fail__when_configuration_endpoint_serves_invalid_xml()
      throws MalformedURLException {
    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    String email = "matej.snuderl@snuderls.test3";
    URL configurationEndpoint = new URL("https://www.w3schools.com/xml/note.xml");
    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String samlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcNCkExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekVOTUFzR0ExVUVDZ3dFVDJ0MFlURVUNCk1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFVEFQQmdOVkJBTU1DSE51ZFdSbGNteHpNUnd3R2dZSktvWklodmNOQVFrQkZnMXANCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQNCkFsVlRNUk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExVFlXNGdSbkpoYm1OcGMyTnZNUTB3Q3dZRFZRUUsNCkRBUlBhM1JoTVJRd0VnWURWUVFMREF0VFUwOVFjbTkyYVdSbGNqRVJNQThHQTFVRUF3d0ljMjUxWkdWeWJITXhIREFhQmdrcWhraUcNCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzANCnhGM0dnUm51Z1ZZVmw1bUFDZHdLZSszc05meERBbUh6emF2N1MxakszM0s0cUN3bmVuWXl4QkVwZ1hJUmNyUEpqNEtWc2tzZUplZlkNCnYrOGpXdTJ0cWFlY0tQRUJDd1ZxSC9NcnI3Qld6QW9jTDlHaUx5NjQvY2RSZ2dQVkRNOGxGV1pkdXYvajJlMDVNemQ5a2xFdUtOaWENCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkNCkZjOUR1Q0tqM1ZJWVBWenRkUWlaRDRJa0V1c2cwc2RQdTB2YmhGcGNablBCUmxrWmM2WE9OSm1vQlNTbnRYQmNQQS9VdGlxTFdKb3MNCkE0R095Q3RFR2tQcE5uYi9CdUJWUzBZa0hWeUgrM1Z2QWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBR2dML3pMb29RaCsNCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMNCjNHejFQZzNVNXA5SFkra3puZ0hsQXAyYWo1ZlBEcVNLK1ppakhnQWxNVlBldUZHYklKZXJUaFhLRlFqWXdBOVZoSVlaTEhDaHJieWYNCjB2OFdnc0JEZFE3aWJzcW1XaTcwRlBmb0hYU0tpWWZUMUxlTE1XcWVlSUJtMzJtMUFCWDNDQXcxZ1lHZlVBWlhINVBYQS80OEwzUXUNCnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXENClU1Zmlzd3BlNlNydHNiVkF2SDFXRzk5am1Ubz08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDJwOlN0YXR1cyB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwycDpTdGF0dXNDb2RlIFZhbHVlPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6c3RhdHVzOlN1Y2Nlc3MiLz48L3NhbWwycDpTdGF0dXM+PHNhbWwyOkFzc2VydGlvbiBJRD0iaWQ1MzI1NjIzNTM1MDgxMzc1MjA5MTg5MDIyMyIgSXNzdWVJbnN0YW50PSIyMDIwLTA5LTA2VDE4OjUwOjQ4LjQxM1oiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly93d3cub2t0YS5jb20vZXhrdzg0M3RsdWNqTUowa0w0eDY8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+PGRzOlJlZmVyZW5jZSBVUkk9IiNpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyBQcmVmaXhMaXN0PSJ4cyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPnMvc3RWZ3JHYUk0MDVRUU91MUE5dDBzc0NlOU1Idkl1eGJldVY3WGNOeGc9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPlZxNFNDTGIrSEJtUE8ydEpYbFQ1RVZETHhKOVlqUWlTTU1iTzA0d1ltZnVFVGtRdGdZUEhBUEhzRTZBMzh1djgzdnBDS2dUTmh0RzFCR0M3T3JiWUlaaEs5YkF0UWtBNCt3RURKNzdXWERyL3J2ZmhjYUtHYWhDNlM2eEhzd1dmYnEwK2c2SERTajhGVERTYmdqYmp0MVVQdEFQczlMSVBEajVlendaSElUZ0NCMGFGMXRSOUQ5Vk40ejFHK2U2WTRkcmVtRFBRbzA2NExLUTJ5WjVhbnEySG1TMGR2YUJaQVdPQmQxUHIxVkVSc1hoRTZISysxNG8vb1YraUlXeTFhTmZySHNmK1d3cUZHalM2czQrazhMQi9MRTg3YmJRT2YxR0hTekJkMFluQlA3VXZ2UHIwKzNZbUpwV25GUU5KR1huOGVGZ0dWUytNUEtmQXZtcmxFdz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEb0RDQ0FvaWdBd0lCQWdJR0FYUmo3SWUvTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdRTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHDQpBMVVFQ0F3S1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ3d05VMkZ1SUVaeVlXNWphWE5qYnpFTk1Bc0dBMVVFQ2d3RVQydDBZVEVVDQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwDQpibVp2UUc5cmRHRXVZMjl0TUI0WERUSXdNRGt3TmpFME5UWTFPVm9YRFRNd01Ea3dOakUwTlRjMU9Wb3dnWkF4Q3pBSkJnTlZCQVlUDQpBbFZUTVJNd0VRWURWUVFJREFwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSERBMVRZVzRnUm5KaGJtTnBjMk52TVEwd0N3WURWUVFLDQpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHDQo5dzBCQ1FFV0RXbHVabTlBYjJ0MFlTNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1BEdy8wDQp4RjNHZ1JudWdWWVZsNW1BQ2R3S2UrM3NOZnhEQW1IenphdjdTMWpLMzNLNHFDd25lbll5eEJFcGdYSVJjclBKajRLVnNrc2VKZWZZDQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhDQpEdHEzaWxiMGFFaXhtUE5lNElRaTJTT2Zna0Z1SlU3QVBuakNaSkJxVEtGam9mYkdtVEdXeVhxdzdONVZoWThGQm03SHAvWXYwdmJZDQpGYzlEdUNLajNWSVlQVnp0ZFFpWkQ0SWtFdXNnMHNkUHUwdmJoRnBjWm5QQlJsa1pjNlhPTkptb0JTU250WEJjUEEvVXRpcUxXSm9zDQpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrDQpqbEQwRHpsTGRRT2VmMjVnVHhkQ0gvcEc5QTV2ZG9WQ1NkbUM4Z2FKS3A5SHhZVm1zOHpBZ1RmU3BCZ1I5UnpDekJIK2NBWEdTeVRjDQozR3oxUGczVTVwOUhZK2t6bmdIbEFwMmFqNWZQRHFTSytaaWpIZ0FsTVZQZXVGR2JJSmVyVGhYS0ZRall3QTlWaElZWkxIQ2hyYnlmDQowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1DQpzWnY0d01DN0tDRnZzcmpJUlcrQVNWcWdJNlpOb1VWUlhBUlBEQi9GZ2tOaTZGdko4Z0ttUkU4NTBleGk0NisveVJ0b2xQUDM3VkFxDQpVNWZpc3dwZTZTcnRzYlZBdkgxV0c5OWptVG89PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwyOlN1YmplY3QgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+bWF0ZWouc251ZGVybEBzbnVkZXJscy50ZXN0Mzwvc2FtbDI6TmFtZUlEPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiBSZWNpcGllbnQ9Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC92MS9zc28vc2FtbC9jYWxsYmFjayIvPjwvc2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWwyOlN1YmplY3Q+PHNhbWwyOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDIwLTA5LTA2VDE4OjQ1OjQ4LjQxM1oiIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PHNhbWwyOkF1ZGllbmNlPmluc2lnaHQtZGV2PC9zYW1sMjpBdWRpZW5jZT48L3NhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PC9zYW1sMjpDb25kaXRpb25zPjxzYW1sMjpBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMjAtMDktMDZUMTU6MDA6MTkuMzk5WiIgU2Vzc2lvbkluZGV4PSJpZDE1OTk0MTgyNDg0MTEuMTI4OTgwODc0MSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdXRobkNvbnRleHQ+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPnVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0PC9zYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWwyOkF1dGhuQ29udGV4dD48L3NhbWwyOkF1dGhuU3RhdGVtZW50PjxzYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iZ2l2ZW5OYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5NYXRlajwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJmYW1pbHlOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5TbnVkZXJsPC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4=";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Failed to fetch SSO configuration\",\"errors\":{\"configurationEndpoint\":\"Invalid XML\"}}}"));
  }

  @Test
  public void callback__should_sign_up_users__when_valid_saml_response()
      throws MalformedURLException {
    String organizationId = Organization.identifier();
    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    String email = "matej.snuderl@snuderls.eu";
    URL configurationEndpoint =
        new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata");
    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String samlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcKQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQKQWxWVE1STXdFUVlEVlFRSURBcERZV3hwWm05eWJtbGhNUll3RkFZRFZRUUhEQTFUWVc0Z1JuSmhibU5wYzJOdk1RMHdDd1lEVlFRSwpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzAKeEYzR2dSbnVnVllWbDVtQUNkd0tlKzNzTmZ4REFtSHp6YXY3UzFqSzMzSzRxQ3duZW5ZeXhCRXBnWElSY3JQSmo0S1Zza3NlSmVmWQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkKRmM5RHVDS2ozVklZUFZ6dGRRaVpENElrRXVzZzBzZFB1MHZiaEZwY1puUEJSbGtaYzZYT05KbW9CU1NudFhCY1BBL1V0aXFMV0pvcwpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMKM0d6MVBnM1U1cDlIWStrem5nSGxBcDJhajVmUERxU0srWmlqSGdBbE1WUGV1RkdiSUplclRoWEtGUWpZd0E5VmhJWVpMSENocmJ5ZgowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1CnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXEKVTVmaXN3cGU2U3J0c2JWQXZIMVdHOTlqbVRvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6U3RhdHVzIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6QXNzZXJ0aW9uIElEPSJpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTA4MTM3NTIwOTE4OTAyMjMiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+cy9zdFZnckdhSTQwNVFRT3UxQTl0MHNzQ2U5TUh2SXV4YmV1VjdYY054Zz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+VnE0U0NMYitIQm1QTzJ0SlhsVDVFVkRMeEo5WWpRaVNNTWJPMDR3WW1mdUVUa1F0Z1lQSEFQSHNFNkEzOHV2ODN2cENLZ1ROaHRHMUJHQzdPcmJZSVpoSzliQXRRa0E0K3dFREo3N1dYRHIvcnZmaGNhS0dhaEM2UzZ4SHN3V2ZicTArZzZIRFNqOEZURFNiZ2pianQxVVB0QVBzOUxJUERqNWV6d1pISVRnQ0IwYUYxdFI5RDlWTjR6MUcrZTZZNGRyZW1EUFFvMDY0TEtRMnlaNWFucTJIbVMwZHZhQlpBV09CZDFQcjFWRVJzWGhFNkhLKzE0by9vVitpSVd5MWFOZnJIc2YrV3dxRkdqUzZzNCtrOExCL0xFODdiYlFPZjFHSFN6QmQwWW5CUDdVdnZQcjArM1ltSnBXbkZRTkpHWG44ZUZnR1ZTK01QS2ZBdm1ybEV3PT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcKQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQKQWxWVE1STXdFUVlEVlFRSURBcERZV3hwWm05eWJtbGhNUll3RkFZRFZRUUhEQTFUWVc0Z1JuSmhibU5wYzJOdk1RMHdDd1lEVlFRSwpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzAKeEYzR2dSbnVnVllWbDVtQUNkd0tlKzNzTmZ4REFtSHp6YXY3UzFqSzMzSzRxQ3duZW5ZeXhCRXBnWElSY3JQSmo0S1Zza3NlSmVmWQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkKRmM5RHVDS2ozVklZUFZ6dGRRaVpENElrRXVzZzBzZFB1MHZiaEZwY1puUEJSbGtaYzZYT05KbW9CU1NudFhCY1BBL1V0aXFMV0pvcwpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMKM0d6MVBnM1U1cDlIWStrem5nSGxBcDJhajVmUERxU0srWmlqSGdBbE1WUGV1RkdiSUplclRoWEtGUWpZd0E5VmhJWVpMSENocmJ5ZgowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1CnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXEKVTVmaXN3cGU2U3J0c2JWQXZIMVdHOTlqbVRvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMjpTdWJqZWN0IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6TmFtZUlEIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6dW5zcGVjaWZpZWQiPm1hdGVqLnNudWRlcmxAc251ZGVybHMuZXU8L3NhbWwyOk5hbWVJRD48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDZUMTg6NTU6NDguNDEzWiIgUmVjaXBpZW50PSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siLz48L3NhbWwyOlN1YmplY3RDb25maXJtYXRpb24+PC9zYW1sMjpTdWJqZWN0PjxzYW1sMjpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMC0wOS0wNlQxODo0NTo0OC40MTNaIiBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDZUMTg6NTU6NDguNDEzWiIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjxzYW1sMjpBdWRpZW5jZT5pbnNpZ2h0LWRldjwvc2FtbDI6QXVkaWVuY2U+PC9zYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjwvc2FtbDI6Q29uZGl0aW9ucz48c2FtbDI6QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDIwLTA5LTA2VDE1OjAwOjE5LjM5OVoiIFNlc3Npb25JbmRleD0iaWQxNTk5NDE4MjQ4NDExLjEyODk4MDg3NDEiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXV0aG5Db250ZXh0PjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMjpBdXRobkNvbnRleHQ+PC9zYW1sMjpBdXRoblN0YXRlbWVudD48c2FtbDI6QXR0cmlidXRlU3RhdGVtZW50IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXR0cmlidXRlIE5hbWU9ImdpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1bnNwZWNpZmllZCI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+TWF0ZWo8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iZmFtaWx5TmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1bnNwZWNpZmllZCI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+U251ZGVybDwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PC9zYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PC9zYW1sMjpBc3NlcnRpb24+PC9zYW1sMnA6UmVzcG9uc2U+";

    Response response =
        given()
            .when()
            .config(RestAssuredUtils.dontFollowRedirects())
            .formParam("SAMLResponse", samlResponse)
            .formParam("RelayState", state)
            .cookie("state", state)
            .post(callbackUri);
    response.then().statusCode(302).header("Location", Location).cookie(SsoSession.COOKIE_NAME);

    AuthUser user = userDatasource.findUser(email).toCompletableFuture().join().get();
    assertEquals(user.getFullName(), "Matej Snuderl");
    assertEquals(user.getOrganizationId(), organizationId);
    assertEquals(user.getRole(), UserRole.STANDARD);

    String blazSnuderlSamlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDU1NDU1MDY0NjMzMDQ5NDYyMTM0NDMzOTU1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDlUMDg6MzE6MTMuNTQ5WiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTU0NTUwNjQ2MzMwNDk0NjIxMzQ0MzM5NTUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+clhHTGx0blBOYWVYTmxLK1IraVgxeENxV3o0ejdqbFZpbncvWWp0K2VaVT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+TGx6VnljS0k4YjJNRko3NFpjWlZlamk2b3NuVlJuVjFYdWw3NXhVNkpBQm11b3ZGQzd4TENTU3p3Q0ZhUU83Z3ZFRlU4NHNYazUvNU9KNCtLSTNnYVQ2ekNOblZPNDhuNVVVQm4xcmRRbUhGZkRqMUhBUFJqZkl4SnJKdmppZXJxVDFPRm9laHVYaDFLMXR3UW1mblFrNXZFUzRiNDhadmVKSTJBMWNuZ3JGQ1g5NnA4dEFnSTl2WDFmeWx5RVFvcldDY09WU1JiS1pabVhEcU9DNTNGSnJxaFozT0NUVExDL0FHWEpwNGIwMjhGM1ZCOWwya3NBa1NDSEMvQWwzaXhtM3FWVElGelJMbDBHTFovbnNMZHpCQk10Wm1EQW9nZUdiY3B2VjVNYW0vUUdvNkhjSVVtOThSdFZRK2FFMGIvV2lGeVRkclp5UnFtSG52cm9vVm9RPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcKQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQKQWxWVE1STXdFUVlEVlFRSURBcERZV3hwWm05eWJtbGhNUll3RkFZRFZRUUhEQTFUWVc0Z1JuSmhibU5wYzJOdk1RMHdDd1lEVlFRSwpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzAKeEYzR2dSbnVnVllWbDVtQUNkd0tlKzNzTmZ4REFtSHp6YXY3UzFqSzMzSzRxQ3duZW5ZeXhCRXBnWElSY3JQSmo0S1Zza3NlSmVmWQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkKRmM5RHVDS2ozVklZUFZ6dGRRaVpENElrRXVzZzBzZFB1MHZiaEZwY1puUEJSbGtaYzZYT05KbW9CU1NudFhCY1BBL1V0aXFMV0pvcwpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMKM0d6MVBnM1U1cDlIWStrem5nSGxBcDJhajVmUERxU0srWmlqSGdBbE1WUGV1RkdiSUplclRoWEtGUWpZd0E5VmhJWVpMSENocmJ5ZgowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1CnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXEKVTVmaXN3cGU2U3J0c2JWQXZIMVdHOTlqbVRvPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6U3RhdHVzIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6QXNzZXJ0aW9uIElEPSJpZDU1NDU1MDY0NjMzODU2Mjc3ODY0ODc3ODkiIElzc3VlSW5zdGFudD0iMjAyMC0wOS0wOVQwODozMToxMy41NDlaIiBWZXJzaW9uPSIyLjAiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiPjxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDplbnRpdHkiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5odHRwOi8vd3d3Lm9rdGEuY29tL2V4a3c4NDN0bHVjak1KMGtMNHg2PC9zYW1sMjpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTI1NiIvPjxkczpSZWZlcmVuY2UgVVJJPSIjaWQ1NTQ1NTA2NDYzMzg1NjI3Nzg2NDg3Nzg5Ij48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyBQcmVmaXhMaXN0PSJ4cyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPnJxUmhKd0hJVmIyMjZZS2ZaTHVPNnpsdlN1ZlJaVXdEeWlKNUl3TGJhVkU9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPkFxS3pTWW5ud2E1VXRFaTc5WXdsUG0veWRzTUVuUzlkUGtiYU13dEUvdWMweTNBNEdzOXMwTzlWKzRzemdWdURrM2grc2NvRnRUdFdJSUpXSlVjZTVlZ2RWOXFhelV3S2R1OThIR3ZPRWprMW81RUtmd0UyQTZHQlNlemV1ZWxULy9pR2pLcEpKVC8rVm9ISktSdjE1cW5wRVcyYnU4TDNTNzFGeG9saDliQzJ6WlRNWlNhMHNtVHZlSjlzZWZNRXRNSmlOYkJwMW1MbFJLWXNXcEhPdzF6b2lKdXllZTRKaFc1c0pDUldYb1dkYVd5Qjk4NDJxamVCUTVXc25YbHJQRmdSVUZnYmJuVmxUNEpUM1lkNW5aMjdFM0JyL1pJVzFmelp5SS9LR3l0RURPZVp2OWVPcUxBU2hOSnRrbHN0Y0RVY0pmOTVDMnQrZ3p6V2N3NEh3Zz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEb0RDQ0FvaWdBd0lCQWdJR0FYUmo3SWUvTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdRTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHCkExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekVOTUFzR0ExVUVDZ3dFVDJ0MFlURVUKTUJJR0ExVUVDd3dMVTFOUFVISnZkbWxrWlhJeEVUQVBCZ05WQkFNTUNITnVkV1JsY214ek1Sd3dHZ1lKS29aSWh2Y05BUWtCRmcxcApibVp2UUc5cmRHRXVZMjl0TUI0WERUSXdNRGt3TmpFME5UWTFPVm9YRFRNd01Ea3dOakUwTlRjMU9Wb3dnWkF4Q3pBSkJnTlZCQVlUCkFsVlRNUk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExVFlXNGdSbkpoYm1OcGMyTnZNUTB3Q3dZRFZRUUsKREFSUGEzUmhNUlF3RWdZRFZRUUxEQXRUVTA5UWNtOTJhV1JsY2pFUk1BOEdBMVVFQXd3SWMyNTFaR1Z5YkhNeEhEQWFCZ2txaGtpRwo5dzBCQ1FFV0RXbHVabTlBYjJ0MFlTNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1BEdy8wCnhGM0dnUm51Z1ZZVmw1bUFDZHdLZSszc05meERBbUh6emF2N1MxakszM0s0cUN3bmVuWXl4QkVwZ1hJUmNyUEpqNEtWc2tzZUplZlkKdis4ald1MnRxYWVjS1BFQkN3VnFIL01ycjdCV3pBb2NMOUdpTHk2NC9jZFJnZ1BWRE04bEZXWmR1di9qMmUwNU16ZDlrbEV1S05pYQpEdHEzaWxiMGFFaXhtUE5lNElRaTJTT2Zna0Z1SlU3QVBuakNaSkJxVEtGam9mYkdtVEdXeVhxdzdONVZoWThGQm03SHAvWXYwdmJZCkZjOUR1Q0tqM1ZJWVBWenRkUWlaRDRJa0V1c2cwc2RQdTB2YmhGcGNablBCUmxrWmM2WE9OSm1vQlNTbnRYQmNQQS9VdGlxTFdKb3MKQTRHT3lDdEVHa1BwTm5iL0J1QlZTMFlrSFZ5SCszVnZBZ01CQUFFd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFHZ0wvekxvb1FoKwpqbEQwRHpsTGRRT2VmMjVnVHhkQ0gvcEc5QTV2ZG9WQ1NkbUM4Z2FKS3A5SHhZVm1zOHpBZ1RmU3BCZ1I5UnpDekJIK2NBWEdTeVRjCjNHejFQZzNVNXA5SFkra3puZ0hsQXAyYWo1ZlBEcVNLK1ppakhnQWxNVlBldUZHYklKZXJUaFhLRlFqWXdBOVZoSVlaTEhDaHJieWYKMHY4V2dzQkRkUTdpYnNxbVdpNzBGUGZvSFhTS2lZZlQxTGVMTVdxZWVJQm0zMm0xQUJYM0NBdzFnWUdmVUFaWEg1UFhBLzQ4TDNRdQpzWnY0d01DN0tDRnZzcmpJUlcrQVNWcWdJNlpOb1VWUlhBUlBEQi9GZ2tOaTZGdko4Z0ttUkU4NTBleGk0NisveVJ0b2xQUDM3VkFxClU1Zmlzd3BlNlNydHNiVkF2SDFXRzk5am1Ubz08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDI6U3ViamVjdCB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOk5hbWVJRCBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjEuMTpuYW1laWQtZm9ybWF0OnVuc3BlY2lmaWVkIj5ibGF6LnNudWRlcmxAc251ZGVybHMuZXU8L3NhbWwyOk5hbWVJRD48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDlUMDg6MzY6MTMuNTQ5WiIgUmVjaXBpZW50PSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siLz48L3NhbWwyOlN1YmplY3RDb25maXJtYXRpb24+PC9zYW1sMjpTdWJqZWN0PjxzYW1sMjpDb25kaXRpb25zIE5vdEJlZm9yZT0iMjAyMC0wOS0wOVQwODoyNjoxMy41NDlaIiBOb3RPbk9yQWZ0ZXI9IjIwMjAtMDktMDlUMDg6MzY6MTMuNTQ5WiIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjxzYW1sMjpBdWRpZW5jZT5pbnNpZ2h0LWRldjwvc2FtbDI6QXVkaWVuY2U+PC9zYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPjwvc2FtbDI6Q29uZGl0aW9ucz48c2FtbDI6QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDIwLTA5LTA5VDA4OjMxOjEyLjk0NloiIFNlc3Npb25JbmRleD0iaWQxNTk5NjQwMjczNTQ3LjE1Mzg4NjI1MzciIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXV0aG5Db250ZXh0PjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMjpBdXRobkNvbnRleHQ+PC9zYW1sMjpBdXRoblN0YXRlbWVudD48c2FtbDI6QXR0cmlidXRlU3RhdGVtZW50IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FtbDI6QXR0cmlidXRlIE5hbWU9ImdpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1bnNwZWNpZmllZCI+PHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+Qmxhejwvc2FtbDI6QXR0cmlidXRlVmFsdWU+PC9zYW1sMjpBdHRyaWJ1dGU+PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJmYW1pbHlOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5TbnVkZXJsPC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4=";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", blazSnuderlSamlResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(302)
        .header("Location", Location)
        .cookie(SsoSession.COOKIE_NAME);

    AuthUser newUser =
        userDatasource.findUser("blaz.snuderl@snuderls.eu").toCompletableFuture().join().get();
    assertEquals(newUser.getFullName(), "Blaz Snuderl");
    assertEquals(newUser.getOrganizationId(), organizationId);
    assertEquals(newUser.getRole(), UserRole.STANDARD);
  }

  @Test
  public void callback__should_fail__when_invalid_signature() throws MalformedURLException {
    String organizationId = Organization.identifier();
    String email = "test.user@snuderls.euu";

    URL configurationEndpoint =
        new URL("https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata");

    Organization organization =
        organizationDatasource
            .createOrganization(organizationId, "Test")
            .toCompletableFuture()
            .join();

    ssoSetupDatasource
        .create(
            new CreateSsoSetup(
                organization.getId(),
                EmailUtils.domainFromEmail(email),
                SsoMethod.SAML,
                configurationEndpoint))
        .toCompletableFuture()
        .join();

    String Location = "https://www.insight.io/my_path";
    String state = samlService.secureState(Location);
    String samlResponse =
        "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvdjEvc3NvL3NhbWwvY2FsbGJhY2siIElEPSJpZDUzMjU2MjM1MzUwMDQ0NDcxODAyNDY1MjI1IiBJc3N1ZUluc3RhbnQ9IjIwMjAtMDktMDZUMTg6NTA6NDguNDEzWiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5IiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGt3ODQzdGx1Y2pNSjBrTDR4Njwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIFVSST0iI2lkNTMyNTYyMzUzNTAwNDQ0NzE4MDI0NjUyMjUiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiPjxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIFByZWZpeExpc3Q9InhzIiB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm0+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+Ulp0TjJhRmU5UFVnc29LQ2N6Vjg4Ui80ZWp0b1BYaU4wUHlOdG03Nzkzdz08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WXBIUjBGVTJEU1VCRXpOalNYUGx1aVhSYkRQeHZPeVZhRkYzdUlTUXh6L1g1VDAxb25rbmRCR1hPcStMN3FoSEc5UGVYOFloMWo4aTdHTkwvc1JSSkt5YXpuUE5EUVoyandsUXYzUkpKdmtkeVpjbitLNWtidXAyeWJhMnNQMWROMUtpWUZZUUxZL1pteldDZXIwVlVQWFBkMllQaTdWdGVWaldBYWZLdk1UWm9QMkJLeXFybTRSL24wZmppVUZGUHBkdG8xY1BLQmwybWpkcEhWNm0ySUdJY3R1NUtnS3Q3UWZJTXRxaU96NVVQc3hGMk5MU25LVEhCbzg3S1VEaEk2aThDdVRPVkFieWphT3FHU1ZZZnd0SzN2L0IrZzRrYVA0dHFIZGpyeUkxZlhIbmcrOHkwVG1nLzVjQ3VBR1VxRDFBcXo0cWRIdTRWVkQvMkduWHVnPT08L2RzOlNpZ25hdHVyZVZhbHVlPjxkczpLZXlJbmZvPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURvRENDQW9pZ0F3SUJBZ0lHQVhSajdJZS9NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR1FNUXN3Q1FZRFZRUUdFd0pWVXpFVE1CRUcNCkExVUVDQXdLUTJGc2FXWnZjbTVwWVRFV01CUUdBMVVFQnd3TlUyRnVJRVp5WVc1amFYTmpiekVOTUFzR0ExVUVDZ3dFVDJ0MFlURVUNCk1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFVEFQQmdOVkJBTU1DSE51ZFdSbGNteHpNUnd3R2dZSktvWklodmNOQVFrQkZnMXANCmJtWnZRRzlyZEdFdVkyOXRNQjRYRFRJd01Ea3dOakUwTlRZMU9Wb1hEVE13TURrd05qRTBOVGMxT1Zvd2daQXhDekFKQmdOVkJBWVQNCkFsVlRNUk13RVFZRFZRUUlEQXBEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIREExVFlXNGdSbkpoYm1OcGMyTnZNUTB3Q3dZRFZRUUsNCkRBUlBhM1JoTVJRd0VnWURWUVFMREF0VFUwOVFjbTkyYVdSbGNqRVJNQThHQTFVRUF3d0ljMjUxWkdWeWJITXhIREFhQmdrcWhraUcNCjl3MEJDUUVXRFdsdVptOUFiMnQwWVM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDUER3LzANCnhGM0dnUm51Z1ZZVmw1bUFDZHdLZSszc05meERBbUh6emF2N1MxakszM0s0cUN3bmVuWXl4QkVwZ1hJUmNyUEpqNEtWc2tzZUplZlkNCnYrOGpXdTJ0cWFlY0tQRUJDd1ZxSC9NcnI3Qld6QW9jTDlHaUx5NjQvY2RSZ2dQVkRNOGxGV1pkdXYvajJlMDVNemQ5a2xFdUtOaWENCkR0cTNpbGIwYUVpeG1QTmU0SVFpMlNPZmdrRnVKVTdBUG5qQ1pKQnFUS0Zqb2ZiR21UR1d5WHF3N041VmhZOEZCbTdIcC9ZdjB2YlkNCkZjOUR1Q0tqM1ZJWVBWenRkUWlaRDRJa0V1c2cwc2RQdTB2YmhGcGNablBCUmxrWmM2WE9OSm1vQlNTbnRYQmNQQS9VdGlxTFdKb3MNCkE0R095Q3RFR2tQcE5uYi9CdUJWUzBZa0hWeUgrM1Z2QWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBR2dML3pMb29RaCsNCmpsRDBEemxMZFFPZWYyNWdUeGRDSC9wRzlBNXZkb1ZDU2RtQzhnYUpLcDlIeFlWbXM4ekFnVGZTcEJnUjlSekN6QkgrY0FYR1N5VGMNCjNHejFQZzNVNXA5SFkra3puZ0hsQXAyYWo1ZlBEcVNLK1ppakhnQWxNVlBldUZHYklKZXJUaFhLRlFqWXdBOVZoSVlaTEhDaHJieWYNCjB2OFdnc0JEZFE3aWJzcW1XaTcwRlBmb0hYU0tpWWZUMUxlTE1XcWVlSUJtMzJtMUFCWDNDQXcxZ1lHZlVBWlhINVBYQS80OEwzUXUNCnNadjR3TUM3S0NGdnNyaklSVytBU1ZxZ0k2Wk5vVVZSWEFSUERCL0Zna05pNkZ2SjhnS21SRTg1MGV4aTQ2Ky95UnRvbFBQMzdWQXENClU1Zmlzd3BlNlNydHNiVkF2SDFXRzk5am1Ubz08L2RzOlg1MDlDZXJ0aWZpY2F0ZT48L2RzOlg1MDlEYXRhPjwvZHM6S2V5SW5mbz48L2RzOlNpZ25hdHVyZT48c2FtbDJwOlN0YXR1cyB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PHNhbWwycDpTdGF0dXNDb2RlIFZhbHVlPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6c3RhdHVzOlN1Y2Nlc3MiLz48L3NhbWwycDpTdGF0dXM+PHNhbWwyOkFzc2VydGlvbiBJRD0iaWQ1MzI1NjIzNTM1MDgxMzc1MjA5MTg5MDIyMyIgSXNzdWVJbnN0YW50PSIyMDIwLTA5LTA2VDE4OjUwOjQ4LjQxM1oiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly93d3cub2t0YS5jb20vZXhrdzg0M3RsdWNqTUowa0w0eDY8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+PGRzOlJlZmVyZW5jZSBVUkk9IiNpZDUzMjU2MjM1MzUwODEzNzUyMDkxODkwMjIzIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyBQcmVmaXhMaXN0PSJ4cyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPnMvc3RWZ3JHYUk0MDVRUU91MUE5dDBzc0NlOU1Idkl1eGJldVY3WGNOeGc9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPlZxNFNDTGIrSEJtUE8ydEpYbFQ1RVZETHhKOVlqUWlTTU1iTzA0d1ltZnVFVGtRdGdZUEhBUEhzRTZBMzh1djgzdnBDS2dUTmh0RzFCR0M3T3JiWUlaaEs5YkF0UWtBNCt3RURKNzdXWERyL3J2ZmhjYUtHYWhDNlM2eEhzd1dmYnEwK2c2SERTajhGVERTYmdqYmp0MVVQdEFQczlMSVBEajVlendaSElUZ0NCMGFGMXRSOUQ5Vk40ejFHK2U2WTRkcmVtRFBRbzA2NExLUTJ5WjVhbnEySG1TMGR2YUJaQVdPQmQxUHIxVkVSc1hoRTZISysxNG8vb1YraUlXeTFhTmZySHNmK1d3cUZHalM2czQrazhMQi9MRTg3YmJRT2YxR0hTekJkMFluQlA3VXZ2UHIwKzNZbUpwV25GUU5KR1huOGVGZ0dWUytNUEtmQXZtcmxFdz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEb0RDQ0FvaWdBd0lCQWdJR0FYUmo3SWUvTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdRTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHDQpBMVVFQ0F3S1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ3d05VMkZ1SUVaeVlXNWphWE5qYnpFTk1Bc0dBMVVFQ2d3RVQydDBZVEVVDQpNQklHQTFVRUN3d0xVMU5QVUhKdmRtbGtaWEl4RVRBUEJnTlZCQU1NQ0hOdWRXUmxjbXh6TVJ3d0dnWUpLb1pJaHZjTkFRa0JGZzFwDQpibVp2UUc5cmRHRXVZMjl0TUI0WERUSXdNRGt3TmpFME5UWTFPVm9YRFRNd01Ea3dOakUwTlRjMU9Wb3dnWkF4Q3pBSkJnTlZCQVlUDQpBbFZUTVJNd0VRWURWUVFJREFwRFlXeHBabTl5Ym1saE1SWXdGQVlEVlFRSERBMVRZVzRnUm5KaGJtTnBjMk52TVEwd0N3WURWUVFLDQpEQVJQYTNSaE1SUXdFZ1lEVlFRTERBdFRVMDlRY205MmFXUmxjakVSTUE4R0ExVUVBd3dJYzI1MVpHVnliSE14SERBYUJna3Foa2lHDQo5dzBCQ1FFV0RXbHVabTlBYjJ0MFlTNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1BEdy8wDQp4RjNHZ1JudWdWWVZsNW1BQ2R3S2UrM3NOZnhEQW1IenphdjdTMWpLMzNLNHFDd25lbll5eEJFcGdYSVJjclBKajRLVnNrc2VKZWZZDQp2KzhqV3UydHFhZWNLUEVCQ3dWcUgvTXJyN0JXekFvY0w5R2lMeTY0L2NkUmdnUFZETThsRldaZHV2L2oyZTA1TXpkOWtsRXVLTmlhDQpEdHEzaWxiMGFFaXhtUE5lNElRaTJTT2Zna0Z1SlU3QVBuakNaSkJxVEtGam9mYkdtVEdXeVhxdzdONVZoWThGQm03SHAvWXYwdmJZDQpGYzlEdUNLajNWSVlQVnp0ZFFpWkQ0SWtFdXNnMHNkUHUwdmJoRnBjWm5QQlJsa1pjNlhPTkptb0JTU250WEJjUEEvVXRpcUxXSm9zDQpBNEdPeUN0RUdrUHBObmIvQnVCVlMwWWtIVnlIKzNWdkFnTUJBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUdnTC96TG9vUWgrDQpqbEQwRHpsTGRRT2VmMjVnVHhkQ0gvcEc5QTV2ZG9WQ1NkbUM4Z2FKS3A5SHhZVm1zOHpBZ1RmU3BCZ1I5UnpDekJIK2NBWEdTeVRjDQozR3oxUGczVTVwOUhZK2t6bmdIbEFwMmFqNWZQRHFTSytaaWpIZ0FsTVZQZXVGR2JJSmVyVGhYS0ZRall3QTlWaElZWkxIQ2hyYnlmDQowdjhXZ3NCRGRRN2lic3FtV2k3MEZQZm9IWFNLaVlmVDFMZUxNV3FlZUlCbTMybTFBQlgzQ0F3MWdZR2ZVQVpYSDVQWEEvNDhMM1F1DQpzWnY0d01DN0tDRnZzcmpJUlcrQVNWcWdJNlpOb1VWUlhBUlBEQi9GZ2tOaTZGdko4Z0ttUkU4NTBleGk0NisveVJ0b2xQUDM3VkFxDQpVNWZpc3dwZTZTcnRzYlZBdkgxV0c5OWptVG89PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwyOlN1YmplY3QgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+Ymxhei5zbnVkZXJsQHNudWRlcmxzLmV1dTwvc2FtbDI6TmFtZUlEPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiBSZWNpcGllbnQ9Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC92MS9zc28vc2FtbC9jYWxsYmFjayIvPjwvc2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWwyOlN1YmplY3Q+PHNhbWwyOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDIwLTA5LTA2VDE4OjQ1OjQ4LjQxM1oiIE5vdE9uT3JBZnRlcj0iMjAyMC0wOS0wNlQxODo1NTo0OC40MTNaIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PHNhbWwyOkF1ZGllbmNlPmluc2lnaHQtZGV2PC9zYW1sMjpBdWRpZW5jZT48L3NhbWwyOkF1ZGllbmNlUmVzdHJpY3Rpb24+PC9zYW1sMjpDb25kaXRpb25zPjxzYW1sMjpBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMjAtMDktMDZUMTU6MDA6MTkuMzk5WiIgU2Vzc2lvbkluZGV4PSJpZDE1OTk0MTgyNDg0MTEuMTI4OTgwODc0MSIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdXRobkNvbnRleHQ+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPnVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0PC9zYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWwyOkF1dGhuQ29udGV4dD48L3NhbWwyOkF1dGhuU3RhdGVtZW50PjxzYW1sMjpBdHRyaWJ1dGVTdGF0ZW1lbnQgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iZ2l2ZW5OYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVuc3BlY2lmaWVkIj48c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj5CbGF6PC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT48L3NhbWwyOkF0dHJpYnV0ZT48c2FtbDI6QXR0cmlidXRlIE5hbWU9ImZhbWlseU5hbWUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dW5zcGVjaWZpZWQiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPlNudWRlcmw8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjwvc2FtbDI6QXR0cmlidXRlU3RhdGVtZW50Pjwvc2FtbDI6QXNzZXJ0aW9uPjwvc2FtbDJwOlJlc3BvbnNlPg==";

    given()
        .when()
        .config(RestAssuredUtils.dontFollowRedirects())
        .formParam("SAMLResponse", samlResponse)
        .formParam("RelayState", state)
        .cookie("state", state)
        .post(callbackUri)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Signature cryptographic validation not successful\"}}"));
  }
}
