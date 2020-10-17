package com.meemaw.test.setup;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.extractSessionCookie;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.OrganizationDTO;
import com.meemaw.auth.organization.resource.v1.OrganizationResource;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.resource.v1.SsoSetupResource;
import com.meemaw.auth.sso.token.resource.v1.AuthTokenResource;
import com.meemaw.auth.user.model.dto.PhoneNumberDTO;
import com.meemaw.auth.user.model.dto.SessionInfoDTO;
import com.meemaw.shared.rest.response.DataResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

public class AuthApiTestProvider {

  public static final String INSIGHT_ADMIN_EMAIL = "admin@insight.io";
  public static final String INSIGHT_ADMIN_FULL_NAME = "Admin Admin";
  public static final String INSIGHT_ADMIN_PASSWORD = "superDuperPassword123";
  public static final UUID INSIGHT_ADMIN_ID =
      UUID.fromString("7c071176-d186-40ac-aaf8-ac9779ab047b");

  private final String baseURI;
  private final ObjectMapper objectMapper;
  private final Function<String, String> signUpConfirmationLinkProvider;

  public AuthApiTestProvider(
      @Nullable String baseURI,
      ObjectMapper objectMapper,
      Function<String, String> signUpConfirmationLinkProvider) {
    this.baseURI = baseURI;
    this.objectMapper = objectMapper;
    this.signUpConfirmationLinkProvider = signUpConfirmationLinkProvider;
  }

  private String resourcePath(String path) {
    return Optional.ofNullable(baseURI).orElse("") + path;
  }

  public SignUpRequestDTO signUpRequestMock(String email, String password) {
    return signUpRequestMock(email, password, null);
  }

  public SignUpRequestDTO signUpRequestMock(
      String email, String password, PhoneNumberDTO phoneNumber) {
    return new SignUpRequestDTO(email, password, "Marko Novak", "Insight", phoneNumber);
  }

  public String signUpAndLoginWithRandomCredentials() throws JsonProcessingException {
    return signUpAndLoginWithRandomCredentials(null);
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

    Response response =
        given().when().get(signUpConfirmationLinkProvider.apply(signUpRequest.getEmail()));

    response.then().statusCode(204).cookie(SsoSession.COOKIE_NAME);

    return extractSessionCookie(response).getValue();
  }

  public String loginWithInsightAdmin() {
    return login(INSIGHT_ADMIN_EMAIL, INSIGHT_ADMIN_PASSWORD);
  }

  public Optional<Organization> getOrganization(String sessionId) {
    String organizationURI = resourcePath(OrganizationResource.PATH);

    DataResponse<OrganizationDTO> dataResponse =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .get(organizationURI)
            .as(new TypeRef<>() {});

    if (dataResponse.getError() != null) {
      return Optional.empty();
    }

    return Optional.of(dataResponse.getData());
  }

  public Optional<SessionInfoDTO> getSessionInfo(String sessionId) {
    String uri = resourcePath(SsoSessionResource.PATH + "/session");

    DataResponse<SessionInfoDTO> dataResponse =
        given().cookie(SsoSession.COOKIE_NAME, sessionId).when().get(uri).as(new TypeRef<>() {});

    if (dataResponse.getError() != null) {
      return Optional.empty();
    }

    return Optional.of(dataResponse.getData());
  }

  public String login(String email, String password) {
    String loginURI = resourcePath(String.join("/", SsoSessionResource.PATH, "login"));
    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .header("referer", "http://localhost:3000")
            .post(loginURI);

    response
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\": true}"))
        .cookie(SsoSession.COOKIE_NAME);

    return extractSessionCookie(response).getValue();
  }

  public String createAuthToken(String sessionId) {
    String resourceURI = resourcePath(AuthTokenResource.PATH);
    DataResponse<Map<String, Object>> response =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .post(resourceURI)
            .as(new TypeRef<>() {});

    return (String) response.getData().get("token");
  }

  public Response setupSso(String sessionId, CreateSsoSetupDTO createSsoSetup)
      throws JsonProcessingException {
    return given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(createSsoSetup))
        .post(resourcePath(SsoSetupResource.PATH))
        .then()
        .statusCode(201)
        .extract()
        .response();
  }

  public Response updateOrganization(String sessionId, Map<String, Object> update)
      throws JsonProcessingException {
    return given()
        .when()
        .contentType(ContentType.JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(objectMapper.writeValueAsString(update))
        .patch(resourcePath(OrganizationResource.PATH))
        .then()
        .statusCode(200)
        .extract()
        .response();
  }
}
