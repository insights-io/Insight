package com.meemaw.testcontainers;

import com.meemaw.resource.v1.beacon.BeaconResourceImplTest;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;

import javax.ws.rs.POST;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PostgresExtension implements BeforeAllCallback {

    private static final Logger log = LoggerFactory.getLogger(PostgresExtension.class);

    @Container
    private static PostgresSQLTestContainer POSTGRES = PostgresSQLTestContainer.newInstance();


    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!POSTGRES.isCreated()) {
            log.info("Starting PostgresSQLContainer");
            POSTGRES.start();
        }

        Path sqlFolder = Path.of(BeaconResourceImplTest.class.getResource(
                "/sql").toURI());

        Files.walk(sqlFolder).filter(path -> !Files.isDirectory(path)).forEach(path -> {
            try {
                POSTGRES.client()
                        .query(Files.readString(path))
                        .toCompletableFuture()
                        .join();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
