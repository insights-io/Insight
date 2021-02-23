package com.rebrowse.test.testconainers.kafka;

import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@Slf4j
public class KafkaMigrationsTestContainer<SELF extends KafkaMigrationsTestContainer<SELF>>
    extends GenericContainer<SELF> {

  public KafkaMigrationsTestContainer(Path path) {
    super(new ImageFromDockerfile().withFileFromPath(".", path));

    withNetwork(Network.SHARED)
        .withCommand("--cluster-config=cluster.yaml apply --skip-confirm topics/*.yaml")
        .withLogConsumer(new Slf4jLogConsumer(log))
        .waitingFor(forLogMessage("^.*Apply completed successfully!.*$", 1));
  }
}
