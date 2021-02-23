package com.rebrowse.auth.utils;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.shared.sms.MockSmsbox;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.resource.v1.UserResource;
import com.rebrowse.model.user.User;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public final class UserFlows extends AbstractTestFlow {

  private final MockSmsbox smsBox;

  private final URI phoneNumberVerifySendCodeEndpoint;
  private final URI phoneNumberVerifyEndpoint;

  public UserFlows(URI baseUri, ObjectMapper objectMapper, MockSmsbox smsBox) {
    super(baseUri, objectMapper);
    this.smsBox = smsBox;
    this.phoneNumberVerifySendCodeEndpoint =
        UriBuilder.fromUri(baseUri)
            .path(UserResource.PATH)
            .path("phone_number/verify/send_code")
            .build();
    this.phoneNumberVerifyEndpoint =
        UriBuilder.fromUri(baseUri).path(UserResource.PATH).path("phone_number/verify").build();
  }

  public void verifyPhoneNumber(User user, String sessionId) throws JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(phoneNumberVerifySendCodeEndpoint)
        .then()
        .statusCode(200);

    int phoneNumberVerificationCode =
        AuthApiTestUtils.getLastSmsMessageVerificationCode(
            smsBox, user.getPhoneNumber().getNumber());

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            objectMapper.writeValueAsString(
                new MfaChallengeCompleteDTO(phoneNumberVerificationCode)))
        .patch(phoneNumberVerifyEndpoint)
        .then()
        .statusCode(200);
  }
}
