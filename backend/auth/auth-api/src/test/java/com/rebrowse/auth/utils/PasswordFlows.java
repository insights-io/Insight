package com.rebrowse.auth.utils;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.auth.password.model.dto.PasswordForgotRequestDTO;
import com.rebrowse.auth.password.resource.v1.PasswordResource;
import com.rebrowse.shared.SharedConstants;
import com.rebrowse.test.utils.GlobalTestData;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import io.restassured.response.Response;
import io.vertx.core.http.HttpHeaders;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public final class PasswordFlows extends AbstractTestFlow {

  private final URI passwordForgotEndpoint;

  public PasswordFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
    this.passwordForgotEndpoint =
        UriBuilder.fromUri(baseUri).path(PasswordResource.PATH).path("forgot").build();
  }

  public Response forgot(String email) throws JsonProcessingException {
    String payload =
        objectMapper.writeValueAsString(
            new PasswordForgotRequestDTO(email, GlobalTestData.LOCALHOST_REDIRECT_URL));

    String referrer = String.format("https://www.%s", SharedConstants.REBROWSE_STAGING_DOMAIN);

    return given()
        .header(HttpHeaders.REFERER.toString(), referrer)
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .post(passwordForgotEndpoint)
        .then()
        .statusCode(204)
        .extract()
        .response();
  }
}
