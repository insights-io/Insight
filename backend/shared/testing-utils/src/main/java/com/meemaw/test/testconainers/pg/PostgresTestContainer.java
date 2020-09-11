package com.meemaw.test.testconainers.pg;

import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ADMIN_EMAIL;
import static com.meemaw.test.setup.SsoTestSetupUtils.INSIGHT_ADMIN_ID;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.shared.sql.SQLContext;
import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.api.Api;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  public static final String NETWORK_ALIAS = "db";

  private static final String DOCKER_TAG = "postgres:12.4";
  private static final String DATABASE_NAME = "postgres";
  private static final String USERNAME = "postgres";
  private static final String PASSWORD = "postgres";
  private static final int PORT = PostgreSQLContainer.POSTGRESQL_PORT;

  private PostgresTestContainer() {
    super(DOCKER_TAG);
  }

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

  public String getDatasourceURL() {
    return getDatasourceURL(getMappedHost());
  }

  public String getDatasourceURL(String host) {
    return String.format("vertx-reactive:postgresql://%s/%s", host, DATABASE_NAME);
  }

  public void applyMigrations() {
    Path moduleSqlMigrationsPath = ProjectUtils.getFromModule("migrations", "postgres");
    applyFlywayMigrations(moduleSqlMigrationsPath);
  }

  public void applyFlywayMigrations(Path moduleSqlMigrationsPath) {
    Path absolutePath = moduleSqlMigrationsPath.toAbsolutePath();
    if (!Files.exists(moduleSqlMigrationsPath)) {
      System.out.println(
          String.format("[TEST-SETUP]: Skipping applyMigrations from=%s", absolutePath));
      return;
    }

    System.out.println(String.format("[TEST-SETUP]: Applying migrations from=%s", absolutePath));
    new PostgresFlywayTestContainer<>(moduleSqlMigrationsPath).start();
    if (moduleSqlMigrationsPath.toAbsolutePath().toString().contains(Api.AUTH.fullName())) {
      createTestUserPassword();
    }
  }

  public void applyMigrationsManually(Path moduleSqlMigrationsPath) {
    Path migrationsSqlPath = Paths.get(moduleSqlMigrationsPath.toString(), "sql");
    Path absolutePath = migrationsSqlPath.toAbsolutePath();

    if (!Files.exists(migrationsSqlPath)) {
      System.out.println(
          String.format("[TEST-SETUP]: Skipping applyMigrations from=%s", absolutePath));
      return;
    }

    System.out.println(String.format("[TEST-SETUP]: Applying migrations from=%s", absolutePath));
    try {
      Files.walk(migrationsSqlPath)
          .filter(path -> !Files.isDirectory(path))
          .forEach(
              path -> {
                System.out.println(String.format("[TEST-SETUP]: Applying migration %s", path));
                try {
                  client().query(Files.readString(path)).executeAndAwait();
                  if ("V1__auth_api_initial.sql".equals(path.getFileName().toString())) {
                    createTestUserPassword();
                  }
                } catch (IOException ex) {
                  System.out.println(
                      String.format("[TEST-SETUP] Failed to apply migration %s", path));
                  throw new RuntimeException(ex);
                }
              });
    } catch (IOException ex) {
      System.out.println(
          String.format(
              "[TEST-SETUP]: Something went wrong while applying migrations from %s",
              absolutePath));
      throw new RuntimeException(ex);
    }
  }

  /**
   * Create test user password so we can use it in our tests. We can't include this into migrations
   * as that would create user in production and expose password to everyone. Insert statement can
   * only be executed after V1__auth_api_initial.sql is applied and auth.user table exists.
   */
  private void createTestUserPassword() {
    System.out.println(
        String.format("[TEST-SETUP]: Creating test password for \"%s\"", INSIGHT_ADMIN_EMAIL));

    Query query =
        SQLContext.POSTGRES
            .insertInto(table("auth.password"))
            .columns(field("user_id", UUID.class), field("hash", String.class))
            .values(
                INSIGHT_ADMIN_ID, "$2a$13$Wr6F0kX3AJQej92nUm.rxuU8S/4.bvQZHeDIcU6X8YxPLT1nNwslS");

    client()
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .executeAndAwait(Tuple.tuple(query.getBindValues()));
  }
}