package com.meemaw.test.testconainers.pg;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class PostgresSQLTestContainer extends PostgreSQLContainer<PostgresSQLTestContainer> {

  private static final String DOCKER_TAG = "postgres:11.6";
  private static final String HOST = "localhost";
  private static final String DATABASE_NAME = "postgres";
  private static final String USERNAME = "postgres";
  private static final String PASSWORD = "postgres";
  private static final int PORT = PostgreSQLContainer.POSTGRESQL_PORT;

  private PostgresSQLTestContainer() {
    super(DOCKER_TAG);
  }

  public static PostgresSQLTestContainer newInstance() {
    return new PostgresSQLTestContainer()
        .withDatabaseName(DATABASE_NAME)
        .withUsername(USERNAME)
        .withPassword(PASSWORD)
        .withExposedPorts(PORT);
  }

  public PgPool client() {
    return PostgresSQLTestContainer.client(this);
  }

  public static PgPool client(PostgreSQLContainer<PostgresSQLTestContainer> container) {
    PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(container.getMappedPort(PORT))
        .setHost(HOST)
        .setDatabase(container.getDatabaseName())
        .setUser(container.getUsername())
        .setPassword(container.getPassword());

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
    return PgPool.pool(connectOptions, poolOptions);
  }


  public String getDatasourceURL() {
    int mappedPort = getMappedPort(PORT);
    return String.format("vertx-reactive:postgresql://%s:%d/%s", HOST, mappedPort, DATABASE_NAME);
  }

  public void applyMigrations() {
    String projectPath = System.getProperty("user.dir");
    Path migrationsSqlPath = Paths.get(projectPath, "migrations", "sql");
    log.info("Applying migrations from {}", migrationsSqlPath.toAbsolutePath());

    try {
      Files.walk(migrationsSqlPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
        log.info("Applying migration {}", path);
        try {
          client()
              .query(Files.readString(path))
              .await()
              .indefinitely();

        } catch (IOException ex) {
          log.error("Failed to apply migration {}", migrationsSqlPath, ex);
          throw new RuntimeException(ex);
        }
      });
    } catch (IOException ex) {
      log.error("Something went wrong while applying migrations from {}",
          migrationsSqlPath.toAbsolutePath(), ex);
      throw new RuntimeException(ex);
    }
  }
}
