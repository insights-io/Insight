package com.meemaw.test.testconainers.kafka;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

@Slf4j
public class KafkaTestContainer extends KafkaContainer {

  public static final String NETWORK_ALIAS = "kafka";

  private static final String CONFLUENT_PLATFORM_VERSION = "5.5.1";

  private KafkaTestContainer() {
    super(CONFLUENT_PLATFORM_VERSION);
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
    log.info("[TEST-SETUP]: Creating kafka topics bootstrap.servers={}", getBootstrapServers());
    List<NewTopic> topics = List.of(new NewTopic("events", 1, (short) 1));

    try {
      adminClient().createTopics(topics).all().get();
    } catch (InterruptedException | ExecutionException ex) {
      ex.printStackTrace();
      log.error("[TEST-SETUP]: Failed to create kafka topics={}", topics, ex);
    }
  }
}
