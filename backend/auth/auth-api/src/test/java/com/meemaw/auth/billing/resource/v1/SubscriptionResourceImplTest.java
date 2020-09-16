package com.meemaw.auth.billing.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.SsoTestSetupUtils.loginWithInsightAdminFromAuthApi;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.billing.model.dto.CreateSubscriptionDTO;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SubscriptionResourceImplTest {

  String eventPath = SubscriptionResource.PATH + "/" + "event";

  @Test
  public void event__should_fail__invalid_content_type() {
    given()
        .when()
        .post(eventPath)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void event__should_fail__on_no_body_and_signature() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(eventPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"stripeSignature\":\"Required\",\"body\":\"Required\"}}}"));
  }

  @Test
  public void event__should_fail__on_random_body_and_signature() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header("Stripe-Signature", "random")
        .body("{}")
        .post(eventPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Unable to extract timestamp and signatures from header\"}}"));
  }

  @Test
  public void create__should_fail__when_invalid_content_type() {
    given()
        .when()
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(415)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":415,\"reason\":\"Unsupported Media Type\",\"message\":\"Media type not supported.\"}}"));
  }

  @Test
  public void create__should_fail__when_not_authenticated() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void create__should_fail__when_missing_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void create__should_fail__when_empty_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body("{}")
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"paymentMethodId\":\"Required\"}}}"));
  }

  @Test
  public void create__should_fail__when_random_payment_id() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, loginWithInsightAdminFromAuthApi())
        .body(JacksonMapper.get().writeValueAsString(new CreateSubscriptionDTO("random")))
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"No such PaymentMethod: 'random'\"}}"));
  }
}
