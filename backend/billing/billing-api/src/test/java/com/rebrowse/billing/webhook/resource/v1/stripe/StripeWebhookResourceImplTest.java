package com.rebrowse.billing.webhook.resource.v1.stripe;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.rebrowse.billing.subscription.resource.v1.SubscriptionResource;
import com.rebrowse.billing.utils.AbstractStripeQuarkusTest;
import com.rebrowse.billing.webhook.service.stripe.StripeWebhookTransformer;
import com.rebrowse.test.testconainers.pg.PostgresTestResource;
import com.stripe.model.Event;
import com.stripe.net.ApiResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTest
@Tag("integration")
public class StripeWebhookResourceImplTest extends AbstractStripeQuarkusTest {

  @Inject StripeWebhookTransformer webhookTransformer;

  String eventPath = SubscriptionResource.PATH + "/event";

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
  public void event__should_fail__when_no_body_and_signature() {
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
  public void event__should_fail__when_random_body_and_signature() {
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
  public void event__should_fail__when_no_signature_match_found_in_payload() {
    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(
            "Stripe-Signature",
            "t=1600283731,v1=f882164c89cc73d2fcebda8d9f28bae66e47f7f995950a39a2d13ee36dec5245,v0=fe8e3f054d5dbd84fc0c841bbd168c8181400b7294d4afa6337038f85043dfb8")
        .body("{}")
        .post(eventPath)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"No signatures found matching the expected signature for payload\"}}"));
  }

  @Test
  public void event__should_pass__when_invoice_paid_that_cannot_be_associated()
      throws URISyntaxException, IOException {
    QuarkusMock.installMockForInstance(new NoVerifyStripeWebhookTransformer(), webhookTransformer);

    String payload =
        Files.readString(
            Path.of(getClass().getResource("/billing/invoice/invoicePaid.json").toURI()));

    given()
        .when()
        .contentType(MediaType.APPLICATION_JSON)
        .header(
            "Stripe-Signature",
            "t=1600283731,v1=f882164c89cc73d2fcebda8d9f28bae66e47f7f995950a39a2d13ee36dec5245,v0=fe8e3f054d5dbd84fc0c841bbd168c8181400b7294d4afa6337038f85043dfb8")
        .body(payload)
        .post(eventPath)
        .then()
        .statusCode(204);
  }

  private static class NoVerifyStripeWebhookTransformer extends StripeWebhookTransformer {

    @Override
    public Event construct(String payload, String signature) {
      return ApiResource.GSON.fromJson(payload, Event.class);
    }
  }
}
