package com.meemaw.test.testconainers.pg;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@Slf4j
public class PostgresFlywayTestContainer<SELF extends PostgresFlywayTestContainer<SELF>>
    extends GenericContainer<SELF> {

  public PostgresFlywayTestContainer(Path migrationsFolderPath) {
    super(new ImageFromDockerfile().withFileFromPath(".", migrationsFolderPath));
    withNetwork(Network.SHARED)
        .withEnv("POSTGRES_HOST", PostgresTestContainer.NETWORK_ALIAS)
        .withLogConsumer(new Slf4jLogConsumer(log));
  }
}
