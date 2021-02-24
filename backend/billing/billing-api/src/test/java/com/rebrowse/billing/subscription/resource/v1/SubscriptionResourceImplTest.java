package com.rebrowse.billing.subscription.resource.v1;

import static com.rebrowse.billing.utils.BillingTestUtils.createVisaTestPaymentMethod;
import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertFalse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.api.query.SortParam;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.billing.subscription.datasource.BillingSubscriptionTable;
import com.rebrowse.billing.subscription.model.SubscriptionPlan;
import com.rebrowse.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.rebrowse.billing.subscription.model.dto.SubscriptionDTO;
import com.rebrowse.billing.utils.AbstractStripeQuarkusTest;
import com.rebrowse.model.billing.Subscription;
import com.rebrowse.model.billing.SubscriptionSearchParams;
import com.rebrowse.model.user.User;
import com.rebrowse.net.RequestOptions;
import com.rebrowse.shared.rest.response.DataResponse;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import com.rebrowse.test.utils.RestAssuredUtils;
import com.stripe.exception.StripeException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class SubscriptionResourceImplTest extends AbstractStripeQuarkusTest {

  @TestHTTPResource URL baseUrl;

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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .post(SubscriptionResource.PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"body\":\"Required\"}}}"));
  }

  @Test
  public void create__should_fail__when_empty_body() {
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = authorizationFlows().loginWithAdminUser();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
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
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    String organizationId =
        authorizationFlows().retrieveUserData(sessionId).getOrganization().getId();

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
    String path = String.format(SubscriptionResource.PATH + "/%s/cancel", UUID.randomUUID());
    RestAssuredUtils.ssoSessionCookieTestCases(Method.PATCH, path);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.PATCH, path);
  }

  @Test
  public void cancel__should_throw_error__when_not_existing_subscription()
      throws JsonProcessingException {
    String path = String.format(SubscriptionResource.PATH + "/%s/cancel", UUID.randomUUID());
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .patch(path)
        .then()
        .statusCode(404)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":404,\"reason\":\"Not Found\",\"message\":\"Not Found\"}}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();

    createSubscription(user, createVisaTestPaymentMethod()).toCompletableFuture().join();

    DataResponse<List<SubscriptionDTO>> listDataResponse =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .get(SubscriptionResource.PATH)
            .as(new TypeRef<>() {});

    SubscriptionDTO subscription = listDataResponse.getData().get(0);
    assertEquals("active", subscription.getStatus());

    String cancelSubscriptionPath =
        SubscriptionResource.PATH + "/" + subscription.getId() + "/cancel";

    DataResponse<SubscriptionDTO> deleteDataResponse =
        given()
            .cookie(SsoSession.COOKIE_NAME, sessionId)
            .when()
            .patch(cancelSubscriptionPath)
            .as(new TypeRef<>() {});

    assertEquals("canceled", deleteDataResponse.getData().getStatus());

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
  public void list__should_returned_sorted_subscriptions__when_sort_by_query()
      throws JsonProcessingException, StripeException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    User user = authorizationFlows().retrieveUserData(sessionId).getUser();

    String apiBaseUrl = baseUrl.toString();
    apiBaseUrl = apiBaseUrl.substring(0, apiBaseUrl.length() - 1);

    RequestOptions requestOptions =
        RequestOptions.sessionId(sessionId).apiBaseUrl(apiBaseUrl).build();

    // Create first subscription
    createSubscription(user, createVisaTestPaymentMethod()).toCompletableFuture().join();

    // Search for created subscription
    Subscription subscription =
        Subscription.search(requestOptions).toCompletableFuture().join().get(0);

    subscription.cancel(requestOptions).toCompletableFuture().join();

    // Create second subscription
    createSubscription(user, createVisaTestPaymentMethod()).toCompletableFuture().join();

    List<Subscription> subscriptions =
        Subscription.search(
                SubscriptionSearchParams.builder()
                    .sortBy(SortParam.asc(BillingSubscriptionTable.CREATED_AT))
                    .build(),
                requestOptions)
            .toCompletableFuture()
            .join();

    assertTrue(subscriptions.get(0).getCreatedAt().isBefore(subscriptions.get(1).getCreatedAt()));

    subscriptions =
        Subscription.search(
                SubscriptionSearchParams.builder()
                    .sortBy(SortParam.desc(BillingSubscriptionTable.CREATED_AT))
                    .build(),
                requestOptions)
            .toCompletableFuture()
            .join();

    assertFalse(subscriptions.get(0).getCreatedAt().isBefore(subscriptions.get(1).getCreatedAt()));
  }

  @Test
  public void list__should_throw_error__when_unauthorized() {
    RestAssuredUtils.ssoSessionCookieTestCases(Method.GET, SubscriptionResource.PATH);
    RestAssuredUtils.ssoBearerTokenTestCases(Method.GET, SubscriptionResource.PATH);
  }

  @Test
  public void list__should_return_empty_collection__when_user_with_no_subscriptions()
      throws JsonProcessingException {
    String sessionId = signUpFlows().signUpAndLoginWithRandomCredentials();
    given()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .when()
        .get(SubscriptionResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));

    String token = authorizationFlows().createApiKey(sessionId).getToken();
    given()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .when()
        .get(SubscriptionResource.PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }
}
