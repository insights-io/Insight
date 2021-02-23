package com.rebrowse.test.utils.auth;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.signup.model.dto.SignUpRequestDTO;
import com.rebrowse.auth.signup.resource.v1.SignUpResource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.test.utils.EmailTestUtils;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.RestAssuredUtils;
import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public final class SignUpFlows {

  private final URI signUpEndpoint;
  private final ObjectMapper objectMapper;
  private final Function<String, String> confirmSignUpLinkProvider;

  public SignUpFlows(
      URI baseUri, ObjectMapper objectMapper, Function<String, String> confirmSignUpLinkProvider) {
    this.objectMapper = objectMapper;
    this.signUpEndpoint = UriBuilder.fromUri(baseUri).path(SignUpResource.PATH).build();
    this.confirmSignUpLinkProvider = confirmSignUpLinkProvider;
  }

  public String signUpAndLogin(String email, String password) throws JsonProcessingException {
    return signUpAndLogin(
        new SignUpRequestDTO(
            GlobalTestData.LOCALHOST_REDIRECT_URL,
            email,
            password,
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            new PhoneNumberDTO("+1", "223344")));
  }

  public String signUpAndLogin(SignUpRequestDTO signUpRequest) throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequest))
        .post(signUpEndpoint)
        .then()
        .statusCode(204);

    return given()
        .config(RestAssuredUtils.dontFollowRedirects())
        .when()
        .get(confirmSignUpLinkProvider.apply(signUpRequest.getEmail()))
        .then()
        .statusCode(302)
        .header("Location", GlobalTestData.LOCALHOST_REDIRECT)
        .cookie(SsoSession.COOKIE_NAME)
        .extract()
        .detailedCookie(SsoSession.COOKIE_NAME)
        .getValue();
  }

  public String signUpAndLoginWithRandomCredentialsNoPhoneNumber() throws JsonProcessingException {
    String email = EmailTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    return signUpAndLogin(
        new SignUpRequestDTO(
            GlobalTestData.LOCALHOST_REDIRECT_URL,
            email,
            password,
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            null));
  }

  public String signUpAndLoginWithRandomCredentials() throws JsonProcessingException {
    String email = EmailTestUtils.randomBusinessEmail();
    String password = UUID.randomUUID().toString();
    return signUpAndLogin(
        new SignUpRequestDTO(
            GlobalTestData.LOCALHOST_REDIRECT_URL,
            email,
            password,
            "Marko Novak",
            SharedConstants.REBROWSE_ORGANIZATION_NAME,
            new PhoneNumberDTO("+1", "223344")));
  }
}
