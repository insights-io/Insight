package com.meemaw.test.testconainers.api;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.pg.PostgresTestContainer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class AbstractApiTestContainer<SELF extends GenericContainer<SELF>>
    extends GenericContainer<SELF> {

  protected final Api api;

  protected static final int PORT = 8080;

  /** @param api */
  public AbstractApiTestContainer(Api api) {
    super(buildDockerImage(Objects.requireNonNull(api)));
    withExposedPorts(PORT)
        .waitingFor(Wait.forHttp("/health").forStatusCode(200))
        .withNetwork(Network.SHARED);
    this.api = api;
  }

  @Override
  public void start() {
    api.dependencies()
        .forEach(
            container -> {
              container.start();
              if (container instanceof PostgresTestContainer) {
                PostgresTestContainer postgresTestContainer = (PostgresTestContainer) container;
                postgresTestContainer.applyMigrations(api.migrations());
                withEnv("POSTGRES_HOST", PostgresTestContainer.NETWORK_ALIAS);
              }
            });
    super.start();
  }

  public String getBaseURI() {
    return String.format("http://%s:%s", getContainerIpAddress(), getPort());
  }

  public int getPort() {
    return getMappedPort(PORT);
  }

  // TODO: use testcontainers for this
  private static String buildDockerImage(Api api) {
    Path dockerfile = api.dockerfile();
    String imageName = api.imageName();
    Path context = ProjectUtils.backendPath();
    log.info(
        "Building {} api dockerfile={} context={} imageName={}",
        api.name().toLowerCase(),
        dockerfile.toString(),
        context.toAbsolutePath(),
        imageName);

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
        log.info(line);
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
}
