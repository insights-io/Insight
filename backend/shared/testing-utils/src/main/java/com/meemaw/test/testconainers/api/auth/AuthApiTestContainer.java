package com.meemaw.test.testconainers.api.auth;

import com.meemaw.test.project.ProjectUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class AuthApiTestContainer extends GenericContainer<AuthApiTestContainer> {

  private static final int PORT = 8080;

  private AuthApiTestContainer() {
    super(dockerImage());
  }

  public static AuthApiTestContainer newInstance() {
    return new AuthApiTestContainer()
        .withExposedPorts(PORT)
        .withLogConsumer(new Slf4jLogConsumer(log))
        .waitingFor(Wait.forHttp("/health").forStatusCode(200));
  }

  // TODO: use testcontainers for this
  private static String dockerImage() {
    Path dockerfile = ProjectUtils.getFromBackend("auth", "api", "docker", "Dockerfile.jvm");
    Path context = ProjectUtils.backendPath();
    String imageName = "auth-api-test";

    log.info(
        "Building auth api dockerfile={} context={} imageName={}",
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
    Process p;
    try {
      p = builder.start();

      BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while (true) {
        line = r.readLine();
        if (line == null) {
          break;
        }
        log.info(line);
      }

      if (p.waitFor() > 0) {
        throw new Exception("failed to build auth api");
      }

      return imageName;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public int getPort() {
    return getMappedPort(PORT);
  }
}
