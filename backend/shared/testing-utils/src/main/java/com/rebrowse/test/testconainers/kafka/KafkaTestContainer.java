package com.rebrowse.test.testconainers.kafka;

import com.rebrowse.test.project.ProjectUtils;
import com.rebrowse.test.testconainers.TestContainerApiDependency;
import com.rebrowse.test.testconainers.api.AbstractApiTestContainer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainer extends KafkaContainer implements TestContainerApiDependency {

  public static final String KAFKA_NETWORK_ALIAS = "kafka";
  public static final String ZOOKEEPER_NETWORK_ALIAS =
      String.format("%s-zookeeper", KAFKA_NETWORK_ALIAS);

  private static final DockerImageName IMAGE_NAME =
      DockerImageName.parse("confluentinc/cp-kafka").withTag("5.5.1");

  private KafkaTestContainer() {
    super(IMAGE_NAME);
    withNetwork(Network.SHARED);
    withNetworkAliases(KAFKA_NETWORK_ALIAS, ZOOKEEPER_NETWORK_ALIAS);
  }

  public static KafkaTestContainer newInstance() {
    return new KafkaTestContainer();
  }

  public static String getDockerBaseUri() {
    return String.format("PLAINTEXT://%s:9092", KAFKA_NETWORK_ALIAS);
  }

  public AdminClient adminClient() {
    return AdminClient.create(
        Collections.singletonMap(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()));
  }

  public void applyMigrations() {
    Path migrationsPath = ProjectUtils.getFromInfrastructure("kafka");
    Path absolutePath = migrationsPath.toAbsolutePath();

    if (!Files.exists(migrationsPath)) {
      System.out.printf("[TEST-SETUP]: Skipping apply kafka migrations from=%s%n", absolutePath);
      return;
    }

    System.out.printf("[TEST-SETUP]: Applying kafka migrations from=%s%n", absolutePath);
    new KafkaMigrationsTestContainer<>(migrationsPath).start();
    System.out.printf(
        "[TEST-SETUP]: Successfully applied kafka migrations from=%s%n", absolutePath);
  }

  @Override
  public void inject(AbstractApiTestContainer<?> container) {
    applyMigrations();
    container.withEnv("KAFKA_BOOTSTRAP_SERVERS", getDockerBaseUri());
  }
}
