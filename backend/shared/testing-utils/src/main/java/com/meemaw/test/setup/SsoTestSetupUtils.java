package com.meemaw.test.setup;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.signup.resource.v1.SignUpResource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.test.testconainers.api.auth.AuthApiTestExtension;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public final class SsoTestSetupUtils {

  private SsoTestSetupUtils() {}

  /**
   * Create a sign up request mock with pre-filled values.
   *
   * @param email String email address
   * @param password String password
   * @return SignUpRequestDTO sign up request
   */
  public static SignUpRequestDTO signUpRequestMock(String email, String password) {
    return new SignUpRequestDTO(email, password, "Marko Novak", "Insight", null);
  }

  /**
   * Sign up as a new user and complete the flow.
   *
   * @param mockMailbox mailbox mock
   * @param objectMapper object mapper
   * @param email String email address
   * @param password String password
   * @return String session id
   * @throws JsonProcessingException if problems serializing
   */
  public static String signUpAndLogin(
      MockMailbox mockMailbox, ObjectMapper objectMapper, String email, String password)
      throws JsonProcessingException {
    return signUpAndLogin(mockMailbox, objectMapper, signUpRequestMock(email, password));
  }

  /**
   * Sign up user and return newly created SessionId.
   *
   * @param mailbox mock mailbox
   * @param objectMapper jackson object mapper
   * @param signUpRequestDTO sign up request data
   * @return String SessionId
   * @throws JsonProcessingException if problems serializing
   */
  public static String signUpAndLogin(
      MockMailbox mailbox, ObjectMapper objectMapper, SignUpRequestDTO signUpRequestDTO)
      throws JsonProcessingException {

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(objectMapper.writeValueAsString(signUpRequestDTO))
        .post(SignUpResource.PATH)
        .then()
        .statusCode(204);

    Response response =
        given()
            .when()
            .get(parseLink(mailbox.getMessagesSentTo(signUpRequestDTO.getEmail()).get(0)));

    response.then().statusCode(204).cookie(SsoSession.COOKIE_NAME);
    return extractSessionCookie(response).getValue();
  }

  /**
   * Parse an anchor element link from mail.
   *
   * @param mail that was sent to the user
   * @return String
   */
  public static String parseLink(Mail mail) {
    Document htmlDocument = Jsoup.parse(mail.getHtml());
    Elements link = htmlDocument.select("a");
    return link.attr("href");
  }

  /**
   * Parse token from confirmation link.
   *
   * @param mail that was sent to the user
   * @return String token
   */
  public static String parseConfirmationToken(Mail mail) {
    Matcher tokenMatcher = Pattern.compile("^.*token=(.*)$").matcher(parseLink(mail));
    tokenMatcher.matches();
    return tokenMatcher.group(1);
  }

  /**
   * Log in with provided credentials. This method can only be used from auth-api as it uses an URL
   * relative to the current environment for login endpoint. If you want to login from other module,
   * use {@link #login(String email, String password, String baseURI)}.
   *
   * @param email address
   * @param password from the user
   * @return session id
   */
  public static String login(String email, String password) {
    return login(email, password, null);
  }

  /**
   * Log in with provided credentials.
   *
   * @param email address
   * @param password from the user
   * @param baseURI auth api base uri
   * @return session id
   */
  public static String login(String email, String password, String baseURI) {
    String uri =
        String.join("/", Optional.ofNullable(baseURI).orElse("") + SsoResource.PATH, "login");

    Response response =
        given()
            .when()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", email)
            .param("password", password)
            .post(uri);

    response.then().statusCode(204).cookie(SsoSession.COOKIE_NAME);
    return extractSessionCookie(response).getValue();
  }

  /**
   * Log in with pre-created Insight admin user.
   *
   * @return session id
   */
  public static String loginWithInsightAdmin() {
    String authApiBaseURI = AuthApiTestExtension.getInstance().getBaseURI();
    return loginWithInsightAdmin(authApiBaseURI);
  }

  /**
   * Log in with pre-created Insight admin user.
   *
   * @param baseURI auth-api base URI
   * @return session id
   */
  public static String loginWithInsightAdmin(String baseURI) {
    return login("admin@insight.io", "superDuperPassword123", baseURI);
  }

  /**
   * Extract session cookie from rest assured response.
   *
   * @param response rest assured response
   * @return Cookie
   */
  public static Cookie extractSessionCookie(Response response) {
    return response.getDetailedCookie(SsoSession.COOKIE_NAME);
  }

  public static void cookieExpect401(String path, String cookie) {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, cookie)
        .get(path)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }
}
