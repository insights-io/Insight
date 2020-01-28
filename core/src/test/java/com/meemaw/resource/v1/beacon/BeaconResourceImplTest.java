package com.meemaw.resource.v1.beacon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.mappers.JacksonMapper;
import com.meemaw.model.beacon.Beacon;
import com.meemaw.model.beacon.BeaconDTO;
import com.meemaw.rest.response.DataResponse;
import com.meemaw.testcontainers.Postgres;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.meemaw.matcher.SameJSON.sameJson;
import static io.restassured.RestAssured.given;


@Testcontainers
@QuarkusTest
@Tag("integration")
public class BeaconResourceImplTest {

    @Container
    public static PostgreSQLContainer DATABASE = Postgres.testContainer();

    @BeforeAll
    public static void init() throws URISyntaxException, IOException {
        Path sqlFolder = Path.of(BeaconResourceImplTest.class.getResource(
                "/sql").toURI());

        Files.walk(sqlFolder).filter(path -> !Files.isDirectory(path)).forEach(path -> {
            try {
                Postgres.client(DATABASE).query(Files.readString(path)).toCompletableFuture().join();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

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
                        "Error\",\"errors\":{\"beacon\":\"Payload may not be blank\"}}}"));
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
