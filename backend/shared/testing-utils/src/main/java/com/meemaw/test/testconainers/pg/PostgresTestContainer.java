package com.meemaw.test.testconainers.pg;

import static com.meemaw.test.setup.AuthApiTestProvider.REBROWSE_ADMIN_EMAIL;
import static com.meemaw.test.setup.AuthApiTestProvider.REBROWSE_ADMIN_ID;
import static com.meemaw.test.setup.AuthApiTestProvider.REBROWSE_ADMIN_PASSWORD;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.meemaw.shared.sql.SQLContext;
import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.TestContainerApiDependency;
import com.meemaw.test.testconainers.api.AbstractApiTestContainer;
import com.meemaw.test.testconainers.api.Api;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.jooq.Query;
import org.jooq.conf.ParamType;
import org.mindrot.jbcrypt.BCrypt;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer>
    implements TestContainerApiDependency {

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

  public PgPool client() {
    return PostgresTestContainer.client(this);
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
      System.out.printf("[TEST-SETUP]: Skipping applyMigrations from=%s%n", absolutePath);
      return;
    }

    System.out.printf("[TEST-SETUP]: Applying migrations from=%s%n", absolutePath);
    new PostgresFlywayTestContainer<>(moduleSqlMigrationsPath).start();
    if (moduleSqlMigrationsPath.toAbsolutePath().toString().contains(Api.AUTH.fullName())) {
      createTestUserPassword();
    }
  }

  /**
   * Create test user password so we can use it in our tests. We can't include this into migrations
   * as that would create user in production and expose password to everyone. Insert statement can
   * only be executed after V1__auth_api_initial.sql is applied and auth.user table exists.
   */
  private void createTestUserPassword() {
    System.out.printf("[TEST-SETUP]: Creating test password for \"%s\"%n", REBROWSE_ADMIN_EMAIL);

    Query query =
        SQLContext.POSTGRES
            .insertInto(table("auth.password"))
            .columns(field("user_id", UUID.class), field("hash", String.class))
            .values(REBROWSE_ADMIN_ID, BCrypt.hashpw(REBROWSE_ADMIN_PASSWORD, BCrypt.gensalt(4)));

    client()
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .executeAndAwait(Tuple.tuple(query.getBindValues()));
  }

  @Override
  public void inject(AbstractApiTestContainer<?> container) {
    applyFlywayMigrations(container.api.pathToPostgresMigrations());
    container.withEnv("POSTGRES_HOST", NETWORK_ALIAS);
  }
}
