package com.meemaw.billing.subscription.resource.v1;

import static com.meemaw.billing.BillingTestUtils.createVisaTestPaymentMethod;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.AbstractStripeTest;
import com.meemaw.billing.service.stripe.StripeBillingService;
import com.meemaw.billing.subscription.model.SubscriptionPlan;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.billing.subscription.model.dto.SubscriptionDTO;
import com.meemaw.shared.rest.response.DataResponse;
import com.meemaw.test.setup.RestAssuredUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class SubscriptionResourceImplTest extends AbstractStripeTest {

  @Inject StripeBillingService billingService;

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
  public void create__should_fail__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(
        Method.POST, SubscriptionResource.PATH, ContentType.JSON);
    RestAssuredUtils.ssoBearerTokenTestCases(
        Method.POST, SubscriptionResource.PATH, ContentType.JSON);
  }

  @Test
  public void create__should_fail__when_missing_body() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
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
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body("{}")
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"paymentMethodId\":\"Required\",\"plan\":\"Required\"}}}"));
  }

  @Test
  public void create__should_fail__when_random_payment_id() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(
            objectMapper.writeValueAsString(
                new CreateSubscriptionDTO("random", SubscriptionPlan.ENTERPRISE)))
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"No such PaymentMethod: 'random'\"}}"));
  }

  @Test
  public void create__should_fail__when_obsolete_payment_id() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(
            objectMapper.writeValueAsString(
                new CreateSubscriptionDTO(
                    "pm_1HS5TUI1ysvdCIIxoLNYYB9S", SubscriptionPlan.ENTERPRISE)))
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"This PaymentMethod was previously used without being attached to a Customer or was detached from a Customer, and may not be used again.\"}}"));
  }

  @Test
  public void create__should_fail__when_free_plan() throws JsonProcessingException {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .body(
            objectMapper.writeValueAsString(
                new CreateSubscriptionDTO("pm_1HS5TUI1ysvdCIIxoLNYYB9S", SubscriptionPlan.FREE)))
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Invalid plan\"}}"));
  }

  @Test
  public void get_plan__should_fail__when_unauthorized() {
    String path = SubscriptionResource.PATH + "/plan";
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, path);
  }

  @Test
  public void get_plan__should_return_enterprise_plan__when_insight() {
    given()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithInsightAdmin())
        .when()
        .get(SubscriptionResource.PATH + "/plan")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":{\"organizationId\":\"000000\",\"type\":\"enterprise\",\"dataRetention\":\"∞\",\"price\":{\"amount\":0,\"interval\":\"month\"}}}"));
  }

  @Test
  public void get_plan__should_return_free_plan__when_no_subscription()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    String organizationId = authApi().getOrganization(sessionId).get().getId();

    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(SubscriptionResource.PATH + "/plan")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\":{\"organizationId\":\"%s\",\"type\":\"free\",\"dataRetention\":\"1m\",\"price\":{\"amount\":0,\"interval\":\"month\"}}}",
                    organizationId)));
  }

  @Test
  public void cancel__should_throw_error__when_unauthorized() {
    String path = SubscriptionResource.PATH + "/random/cancel";
    RestAssuredUtils.ssoSessionCookieTestCases(Method.PATCH, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.PATCH, path);
  }

  @Test
  public void cancel__should_throw_error__when_not_existing_subscription()
      throws JsonProcessingException {
    String path = SubscriptionResource.PATH + "/random/cancel";

    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .patch(path)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .when()
        .patch(path)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));
  }

  @Test
  public void cancel__should_return_free_plan__when_successfully_canceled()
      throws JsonProcessingException, StripeException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    AuthUser user = authApi().getSessionInfo(sessionId).get().getUser();

    PaymentMethod visaTestPaymentMethod = createVisaTestPaymentMethod();
    billingService
        .createSubscription(user, SubscriptionPlan.ENTERPRISE, visaTestPaymentMethod)
        .toCompletableFuture()
        .join();

    DataResponse<List<SubscriptionDTO>> listDataResponse =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .get(SubscriptionResource.PATH)
            .as(new TypeRef<>() {});

    SubscriptionDTO subscription = listDataResponse.getData().get(0);
    Assertions.assertEquals("active", subscription.getStatus());

    String cancelSubscriptionPath =
        SubscriptionResource.PATH + "/" + subscription.getId() + "/cancel";

    DataResponse<SubscriptionDTO> deleteDataResponse =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .patch(cancelSubscriptionPath)
            .as(new TypeRef<>() {});

    Assertions.assertEquals("canceled", deleteDataResponse.getData().getStatus());

    // Trying to cancel same subscription again should throw
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .patch(cancelSubscriptionPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Only active subscription can be canceled\"}}"));

    // get plan should return free plan after subscription canceled
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(SubscriptionResource.PATH + "/plan")
        .then()
        .statusCode(200)
        .body(
            sameJson(
                String.format(
                    "{\"data\":{\"organizationId\":\"%s\",\"type\":\"free\",\"dataRetention\":\"1m\",\"price\":{\"amount\":0,\"interval\":\"month\"}}}",
                    user.getOrganizationId())));
  }

  @Test
  public void list__should_throw_error__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, SubscriptionResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, SubscriptionResource.PATH);
  }

  @Test
  public void list__should_return_empty_collection__when_user_with_no_subscriptions()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(SubscriptionResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    String authToken = authApi().createApiKey(sessionId);
    given()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
        .when()
        .get(SubscriptionResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }
}
