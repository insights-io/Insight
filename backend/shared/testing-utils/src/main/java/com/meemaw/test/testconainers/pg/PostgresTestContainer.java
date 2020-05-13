package com.meemaw.test.testconainers.pg;

import com.meemaw.test.project.ProjectUtils;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  public static final String NETWORK_ALIAS = "db";

  private static final String DOCKER_TAG = "postgres:11.6";
  private static final String DATABASE_NAME = "postgres";
  private static final String USERNAME = "postgres";
  private static final String PASSWORD = "postgres";
  private static final int PORT = PostgreSQLContainer.POSTGRESQL_PORT;

  private PostgresTestContainer() {
    super(DOCKER_TAG);
  }

  /** @return */
  public static PostgresTestContainer newInstance() {
    return new PostgresTestContainer()
        .withNetwork(Network.SHARED)
        .withNetworkAliases(NETWORK_ALIAS)
        .withDatabaseName(DATABASE_NAME)
        .withUsername(USERNAME)
        .withPassword(PASSWORD)
        .withExposedPorts(PORT);
  }

  public PgPool client() {
    return PostgresTestContainer.client(this);
  }

  /**
   * @param container
   * @return
   */
  public static PgPool client(PostgresTestContainer container) {
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setPort(container.getPort())
            .setHost(container.getContainerIpAddress())
            .setDatabase(container.getDatabaseName())
            .setUser(container.getUsername())
            .setPassword(container.getPassword());

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
    return PgPool.pool(connectOptions, poolOptions);
  }

  public int getPort() {
    return getMappedPort(PORT);
  }

  public String getHost(String host) {
    return String.format("%s:%d", host, getPort());
  }

  public String getHost() {
    return getHost(getContainerIpAddress());
  }

  /** @return */
  public String getDatasourceURL() {
    return getDatasourceURL(getHost());
  }

  /**
   * @param host
   * @return
   */
  public String getDatasourceURL(String host) {
    return String.format("vertx-reactive:postgresql://%s/%s", host, DATABASE_NAME);
  }

  public void applyMigrations() {
    Path moduleMigrationsSqlPath = ProjectUtils.getFromModule("migrations", "sql");
    applyMigrations(moduleMigrationsSqlPath);
  }

  /** @param migrationsSqlPath */
  public void applyMigrations(Path migrationsSqlPath) {
    if (!Files.exists(migrationsSqlPath)) {
      log.info("Skipping applyMigrations from {}", migrationsSqlPath.toAbsolutePath());
      return;
    }

    log.info("Applying migrations from {}", migrationsSqlPath.toAbsolutePath());
    try {
      Files.walk(migrationsSqlPath)
          .filter(path -> !Files.isDirectory(path))
          .forEach(
              path -> {
                log.info("Applying migration {}", path);
                try {
                  client().query(Files.readString(path)).await().indefinitely();

                } catch (IOException ex) {
                  log.error("Failed to apply migration {}", migrationsSqlPath, ex);
                  throw new RuntimeException(ex);
                }
              });
    } catch (IOException ex) {
      log.error(
          "Something went wrong while applying migrations from {}",
          migrationsSqlPath.toAbsolutePath(),
          ex);
      throw new RuntimeException(ex);
    }
  }
}
