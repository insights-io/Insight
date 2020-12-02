package com.meemaw.test.testconainers.api;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestContainer;
import com.meemaw.test.testconainers.pg.PostgresTestContainer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class AbstractApiTestContainer<SELF extends GenericContainer<SELF>>
    extends GenericContainer<SELF> {

  protected static final int EXPOSED_PORT = 80;
  protected final Api api;

  public AbstractApiTestContainer(Api api) {
    super(imageFromDockerfile(Objects.requireNonNull(api)));
    withExposedPorts(EXPOSED_PORT)
        .withNetworkAliases(api.fullName())
        .waitingFor(Wait.forHttp("/health").forStatusCode(200))
        .withNetwork(Network.SHARED)
        .withLogConsumer(new Slf4jLogConsumer(log));

    this.api = api;
  }

  // TODO: use testcontainers for this
  private static String imageFromDockerfile(Api api) {
    Path dockerfile = api.dockerfile();
    String imageName = api.imageName();
    Path context = ProjectUtils.getFromBackend();
    System.out.printf(
        "[TEST-SETUP]: Building %s api dockerfile=%s context=%s imageName=%s%n",
        api.name().toLowerCase(), dockerfile.toString(), context.toAbsolutePath(), imageName);

    ProcessBuilder builder =
        new ProcessBuilder(
            "docker",
            "build",
            "-f",
            dockerfile.toString(),
            "-t",
            imageName,
            context.toAbsolutePath().toString());
    builder.redirectErrorStream(true);
    Process process;
    try {
      process = builder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while (true) {
        line = reader.readLine();
        if (line == null) {
          break;
        }
        System.out.println(line);
      }

      if (process.waitFor() > 0) {
        throw new RuntimeException(
            String.format("failed to build %s api", api.name().toLowerCase()));
      }

      return imageName;
    } catch (IOException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String getBaseURI(String host, int port) {
    return String.format("http://%s:%s", host, port);
  }

  public String getBaseURI() {
    return getBaseURI(getContainerIpAddress(), getPort());
  }

  public String getDockerBaseURI() {
    return getBaseURI(api.fullName(), EXPOSED_PORT);
  }

  public int getPort() {
    return getMappedPort(EXPOSED_PORT);
  }

  @Override
  public void start() {
    api.dependencies().forEach(this::startDependantContainer);
    super.start();
  }

  private void startDependantContainer(GenericContainer<?> container) {
    System.out.printf(
        "[TEST-SETUP]: Starting %s as a dependency of %s%n",
        container.getDockerImageName(), api.fullName());

    container.start();

    if (container instanceof PostgresTestContainer) {
      PostgresTestContainer postgresTestContainer = (PostgresTestContainer) container;
      postgresTestContainer.applyFlywayMigrations(api.postgresMigrations());
      withEnv("POSTGRES_HOST", PostgresTestContainer.NETWORK_ALIAS);
    } else if (container instanceof AuthApiTestContainer) {
      AuthApiTestContainer authApiTestContainer = (AuthApiTestContainer) container;
      withEnv("auth-api/mp-rest/url", authApiTestContainer.getDockerBaseURI());
    }
  }
}
