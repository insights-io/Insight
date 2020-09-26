package com.meemaw.billing.invoice.resource.v1;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
@QuarkusTest
@Tag("integration")
public class InvoiceResourceImplTest extends ExternalAuthApiProvidedTest {

  private static final String LIST_INVOICES_PATH =
      InvoiceResource.PATH + "/{subscriptionId}/invoices";

  @Test
  public void list_invoices__should_fail__when_not_authenticated() {
    given()
        .when()
        .pathParam("subscriptionId", "random")
        .get(LIST_INVOICES_PATH)
        .then()
        .statusCode(401)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":401,\"reason\":\"Unauthorized\",\"message\":\"Unauthorized\"}}"));
  }

  @Test
  public void list_invoices__should_return_empty_list__when_random_subscription_id()
      throws JsonProcessingException {
    String sessionId = authApi().signUpAndLoginWithRandomCredentials();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .pathParam("subscriptionId", "random")
        .get(LIST_INVOICES_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[]}"));
  }
}
