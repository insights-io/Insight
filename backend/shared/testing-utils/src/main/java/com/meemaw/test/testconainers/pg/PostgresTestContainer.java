package com.meemaw.test.testconainers.pg;

import com.meemaw.shared.sql.SQLContext;
import com.meemaw.test.project.ProjectUtils;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  public static final String NETWORK_ALIAS = "db";

  private static final String DOCKER_TAG = "postgres:12.3";
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

  public String getMappedHost(String host) {
    return String.format("%s:%d", host, getPort());
  }

  public String getMappedHost() {
    return getMappedHost(getContainerIpAddress());
  }

  /** @return */
  public String getDatasourceURL() {
    return getDatasourceURL(getMappedHost());
  }

  /**
   * @param host datasource host
   * @return datasource URL
   */
  public String getDatasourceURL(String host) {
    return String.format("vertx-reactive:postgresql://%s/%s", host, DATABASE_NAME);
  }

  public void applyMigrations() {
    Path moduleSqlMigrationsPath = ProjectUtils.getFromModule("migrations");
    applyFlywayMigrations(moduleSqlMigrationsPath);
  }

  /**
   * Create test user password so we can use it in our tests. We can't include this into migrations
   * as that would create user in production and expose password to everyone. Insert statement can
   * only be executed after V1__auth_api_initial.sql is applied and auth.user table exists.
   */
  private void maybeCreateTestUserPassword(Path migrationSqlPath) {
    if ("V1__auth_api_initial.sql".equals(migrationSqlPath.getFileName().toString())) {
      log.info("Creating test password for \"admin@insight.io\"");

      Query query =
          SQLContext.POSTGRES
              .insertInto(DSL.table("auth.password"))
              .columns(DSL.field("user_id"), DSL.field("hash"))
              .values(
                  "7c071176-d186-40ac-aaf8-ac9779ab047b",
                  "$2a$13$Wr6F0kX3AJQej92nUm.rxuU8S/4.bvQZHeDIcU6X8YxPLT1nNwslS");

      client()
          .preparedQuery(query.getSQL(ParamType.NAMED))
          .executeAndAwait(Tuple.tuple(query.getBindValues()));
    }
  }

  /**
   * Apply module migrations via Flyway using Dockerfile.
   *
   * @param migrationsSqlPath path to the folder containing the Dockerfile
   */
  public void applyFlywayMigrations(Path migrationsSqlPath) {
    if (!Files.exists(migrationsSqlPath)) {
      log.info("Skipping applyMigrations from {}", migrationsSqlPath.toAbsolutePath());
      return;
    }

    log.info("Applying migrations from {}", migrationsSqlPath.toAbsolutePath());
    new PostgresFlywayTestContainer<>(migrationsSqlPath).start();
  }

  /**
   * Apply module migrations manually by traversing files in the directory.
   *
   * @param migrationsSqlPath path to the folder containing migrations
   */
  public void applyMigrationsManually(Path migrationsSqlPath) {
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
                  client().query(Files.readString(path)).executeAndAwait();
                  maybeCreateTestUserPassword(path);
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
