package com.meemaw.test.testconainers.kafka;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainer extends KafkaContainer {

  public static final String NETWORK_ALIAS = "kafka";

  private static final DockerImageName IMAGE_NAME =
      DockerImageName.parse("confluentinc/cp-kafka").withTag("5.5.1");

  private KafkaTestContainer() {
    super(IMAGE_NAME);
    withNetwork(Network.SHARED);
    withNetworkAliases(NETWORK_ALIAS);
  }

  public static KafkaTestContainer newInstance() {
    return new KafkaTestContainer();
  }

  public AdminClient adminClient() {
    return AdminClient.create(
        Collections.singletonMap(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()));
  }

  public void createTopics() {
    List<NewTopic> topics = List.of(new NewTopic("events", 1, (short) 1));

    System.out.printf(
        "[TEST-SETUP]: Creating kafka topics bootstrap.servers=%s topics=%s%n",
        getBootstrapServers(), topics);

    try {
      adminClient().createTopics(topics).all().get();
    } catch (InterruptedException | ExecutionException ex) {
      System.out.printf("[TEST-SETUP]: Failed to create kafka topics=%s%n", topics);
      throw new RuntimeException(ex);
    }
  }
}
