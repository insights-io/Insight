package com.meemaw.resource.v1.page;

import com.meemaw.testcontainers.Postgres;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.meemaw.matcher.SameJSON.sameJson;
import static io.restassured.RestAssured.given;

@Postgres
@QuarkusTest
@Tag("integration")
public class PageResourceImplTest {

    @Test
    public void postPage_shouldThrowError_whenUnsupportedMediaType() {
        given()
                .when().contentType(MediaType.TEXT_PLAIN).post(PageResource.PATH)
                .then()
                .statusCode(415)
                .body(sameJson("{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media " +
                        "Type\",\"statusCode\":415}}"));
    }

    @Test
    public void postPage_shouldThrowError_whenEmptyPayload() {
        given()
                .when().contentType(MediaType.APPLICATION_JSON).post(PageResource.PATH)
                .then()
                .statusCode(400)
                .body(sameJson("{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation " +
                        "Error\",\"errors\":{\"page\":\"Payload may not be blank\"}}}"));
    }

    @Test
    public void postPage_shouldThrowError_whenEmptyJson() {
        given()
                .when().contentType(MediaType.APPLICATION_JSON).body("{}").post(PageResource.PATH)
                .then()
                .statusCode(400)
                .body(sameJson("{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"doctype\":\"may not be null\",\"referrer\":\"may not be null\",\"org\":\"may not be null\",\"url\":\"may not be null\"}}}"));
    }

    @Test
    public void postPage_shouldProcess_whenValidPayload() throws IOException, URISyntaxException {
        String payload = Files.readString(Path.of(getClass().getResource(
                "/page.json").toURI()));

        given()
                .when().contentType(ContentType.JSON).body(payload).post(PageResource.PATH)
                .then()
                .statusCode(200);
    }
}
