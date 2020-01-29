package com.meemaw.resource.v1.beacon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.mappers.JacksonMapper;
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
public class BeaconResourceImplTest {

    @Test
    public void postBeacon_shouldThrowError_whenUnsupportedMediaType() {
        given()
                .when().contentType(MediaType.TEXT_PLAIN).post(BeaconResource.PATH)
                .then()
                .statusCode(415)
                .body(sameJson("{\"error\":{\"message\":\"Media type not supported.\",\"reason\":\"Unsupported Media " +
                        "Type\",\"statusCode\":415}}"));
    }

    @Test
    public void postBeacon_shouldThrowError_whenEmptyPayload() {
        given()
                .when().contentType(MediaType.APPLICATION_JSON).post(BeaconResource.PATH)
                .then()
                .statusCode(400)
                .body(sameJson("{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation " +
                        "Error\",\"errors\":{\"arg0\":\"Payload may not be blank\"}}}"));
    }

    @Test
    public void postBeacon_shouldThrowError_whenEmptyJson() {
        given()
                .when().contentType(MediaType.APPLICATION_JSON).body("{}").post(BeaconResource.PATH)
                .then()
                .statusCode(400)
                .body(sameJson("{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"sequence\":\"s must be greater than 0\",\"events\":\"e may not be null\"}}}"));
    }

    @Test
    public void postBeaconShouldStore_whenValidPayload() throws IOException, URISyntaxException {
        String payload = Files.readString(Path.of(getClass().getResource(
                "/beacon.json").toURI()));

        ObjectMapper objectMapper = JacksonMapper.get();

        given()
                .when().contentType(ContentType.JSON).body(payload).post(BeaconResource.PATH)
                .then()
                .statusCode(200)
                .body(sameJson("{\"data\":{\"timestamp\":400,\"sequence\":1,\"events\":[]}}"));
    }

}
