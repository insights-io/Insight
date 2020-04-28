package com.meemaw.test.testconainers.service;

import com.meemaw.test.project.ProjectUtils;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

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

  private static ImageFromDockerfile dockerImage() {
    Path dockerfile = ProjectUtils.getFromBackend("auth", "api", "docker", "Dockerfile.jvm");
    Path context = ProjectUtils.backendPath();

    System.out.println("Dockerfile: " + dockerfile.toString());
    System.out.println("Context: " + context.toString());

    return new ImageFromDockerfile()
        .withDockerfile(dockerfile)
        .withFileFromFile(".", context.toFile());
  }

  public int getPort() {
    return getMappedPort(PORT);
  }
}
