package com.meemaw.test.setup;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.shared.SharedConstants;
import com.rebrowse.model.auth.ApiKey;
import com.rebrowse.model.auth.UserData;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.net.RequestOptions;
import io.vertx.core.http.HttpHeaders;
import java.util.UUID;
import java.util.function.Function;
import javax.ws.rs.core.MediaType;

public class AuthApiTestProvider {

  public static final String REBROWSE_ADMIN_EMAIL =
      String.format("admin@%s", SharedConstants.REBROWSE_STAGING_DOMAIN);

  public static final String REBROWSE_ADMIN_FULL_NAME = "Admin Admin";
  public static final String REBROWSE_ADMIN_PASSWORD = "superDuperPassword123";
  public static final UUID REBROWSE_ADMIN_ID =
      UUID.fromString("7c071176-d186-40ac-aaf8-ac9779ab047b");

  private final String baseUrl;
  private final ObjectMapper objectMapper;
  private final Function<String, String> signUpConfirmationLinkProvider;

  public AuthApiTestProvider(
      String baseUrl,
      ObjectMapper objectMapper,
      Function<String, String> signUpConfirmationLinkProvider) {
    this.baseUrl = baseUrl;
    this.objectMapper = objectMapper;
    this.signUpConfirmationLinkProvider = signUpConfirmationLinkProvider;
  }

  private String resourcePath(String path) {
    return baseUrl + path;
  }

  public SignUpRequestDTO signUpRequestMock(String email, String password) {
    return signUpRequestMock(email, password, null);
  }

  public SignUpRequestDTO signUpRequestMock(
      String email, String password, PhoneNumberDTO phoneNumber) {
    return new SignUpRequestDTO(
        email, password, "Marko Novak", SharedConstants.ORGANIZATION_NAME, phoneNumber);
  }

  public String signUpAndLoginWithRandomBusinessCredentials() throws JsonProcessingException {
    return signUpAndLoginWithRandomBusinessCredentials(null);
  }

  public String signUpAndLoginWithRandomBusinessCredentials(PhoneNumberDTO phoneNumber)
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = String.format("%s@%s.com", password, UUID.randomUUID());
    return signUpAndLogin(signUpRequestMock(email, password, phoneNumber));
  }

  public String signUpAndLoginWithRandomCredentials() throws JsonProcessingException {
    return signUpAndLoginWithRandomCredentials(null);
  }

  public String signUpAndLoginWithRandomCredentials(PhoneNumberDTO phoneNumber)
      throws JsonProcessingException {
    String password = UUID.randomUUID().toString();
    String email = password + "@gmail.com";
    return signUpAndLogin(signUpRequestMock(email, password, phoneNumber));
  }

  public String signUpAndLogin(String email, String password, PhoneNumberDTO phoneNumberDTO)
      throws JsonProcessingException {
    return signUpAndLogin(signUpRequestMock(email, password, phoneNumberDTO));
  }

  public String signUpAndLogin(String email, String password) throws JsonProcessingException {
    return signUpAndLogin(signUpRequestMock(email, password));
  }

  public String signUpAndLogin(SignUpRequestDTO signUpRequest) throws JsonProcessingException {
    String signUpURI = resourcePath(SignUpResource.PATH);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequest))
        .post(signUpURI)
        .then()
        .statusCode(204);

    return given()
        .when()
        .get(signUpConfirmationLinkProvider.apply(signUpRequest.getEmail()))
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .extract()
        .detailedCookie(SsoSession.COOKIE_NAME)
        .getValue();
  }

  public String loginWithAdminUser() {
    return login(REBROWSE_ADMIN_EMAIL, REBROWSE_ADMIN_PASSWORD);
  }

  public Organization retrieveOrganization(String sessionId) {
    return Organization.retrieve(sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();
  }

  public UserData retrieveUserData(String sessionId) {
    return UserData.retrieve(sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();
  }

  public String login(String email, String password) {
    String loginURI = resourcePath(String.join("/", SsoSessionResource.PATH, "login"));
    return given()
        .when()
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", email)
        .param("password", password)
        .header(HttpHeaders.REFERER.toString(), "http://localhost:3000")
        .post(loginURI)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME)
        .extract()
        .detailedCookie(SsoSession.COOKIE_NAME)
        .getValue();
  }

  public String createApiKey(String sessionId) {
    return ApiKey.create(sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join()
        .getToken();
  }

  /**
   * Helper method for SDK {@link com.rebrowse.net.RequestOptions.Builder} creation.
   *
   * @return SDK request options builder connected with the auth-api instance in context
   */
  public RequestOptions.Builder sdkRequest() {
    return new RequestOptions.Builder().apiBaseUrl(this.baseUrl);
  }
}
