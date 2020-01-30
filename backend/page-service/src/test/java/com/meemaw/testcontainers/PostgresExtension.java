package com.meemaw.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PostgresExtension implements BeforeAllCallback {

    @Container
    private static PostgresSQLTestContainer POSTGRES = PostgresSQLTestContainer.newInstance();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }

        String projectPath = System.getProperty("user.dir");
        Path migrationsSqlPath = Paths.get(projectPath, "..", "page-service-migrations", "sql");

        Files.walk(migrationsSqlPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
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
