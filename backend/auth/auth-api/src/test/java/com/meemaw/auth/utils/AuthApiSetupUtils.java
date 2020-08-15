package com.meemaw.auth.utils;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import com.meemaw.auth.user.resource.v1.UserTfaResource;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.ws.rs.core.MediaType;

public final class AuthApiSetupUtils {

  private AuthApiSetupUtils() {}

  public static String setupTfa(
      UUID userId, String sessionId, SsoVerificationDatasource verificationDatasource)
      throws GeneralSecurityException, JsonProcessingException {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(200);

    String secret =
        verificationDatasource.getTfaSetupSecret(userId).toCompletableFuture().join().get();
    int tfaCode = (int) TimeBasedOneTimePasswordUtil.generateCurrentNumber(secret);

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .body(JacksonMapper.get().writeValueAsString(new TfaCompleteDTO(tfaCode)))
        .post(UserTfaResource.PATH + "/setup")
        .then()
        .statusCode(200);

    return secret;
  }
}
