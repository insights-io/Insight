package com.meemaw.test.testconainers.elasticsearch;

import static org.testcontainers.containers.wait.strategy.Wait.forLogMessage;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

@Slf4j
public class ElasticsearchMigrationsTestContainer<
        SELF extends ElasticsearchMigrationsTestContainer<SELF>>
    extends GenericContainer<SELF> {

  public ElasticsearchMigrationsTestContainer(Path path) {
    super(new ImageFromDockerfile().withFileFromPath(".", path));
    withNetwork(Network.SHARED)
        .withEnv(
            "ELASTICSEARCH_HOSTS",
            String.format("http://%s:9200", ElasticsearchTestContainer.NETWORK_ALIAS))
        .withLogConsumer(new Slf4jLogConsumer(log))
        .waitingFor(forLogMessage("^.*All done!.*$", 1));
  }
}
