package com.rebrowse.events.search.indexer;

import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.model.incoming.UserEvent;
import com.rebrowse.events.model.incoming.serialization.UserEventDeserializer;
import com.rebrowse.events.model.incoming.serialization.UserEventSerializer;
import com.rebrowse.events.stream.kafka.KafkaSourcedBatchProcessor;
import com.rebrowse.events.stream.kafka.RetryQueueStandaloneKafkaConsumer;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class SearchIndexer {

  private static final String CONSUMER_GROUP_ID = "search-indexer";

  private final KafkaSourcedBatchProcessor<String, UserEvent<AbstractBrowserEvent<?>>> processor;

  /**
   * @param sourceTopicName source topic name
   * @param retryTopicName retry topic name
   * @param deadLetterTopicName dead letter topic name
   * @param bootstrapServers bootstrap servers
   * @param client rest high level client
   */
  public SearchIndexer(
      String sourceTopicName,
      String retryTopicName,
      String deadLetterTopicName,
      String bootstrapServers,
      RestHighLevelClient client) {
    Properties consumerProps = SearchIndexer.consumerProperties(bootstrapServers);
    Properties producerProperties = SearchIndexer.retryQueueProducerProperties(bootstrapServers);

    this.processor =
        new KafkaSourcedBatchProcessor<>(
            new RetryQueueStandaloneKafkaConsumer<>(
                consumerProps,
                producerProperties,
                sourceTopicName,
                retryTopicName,
                deadLetterTopicName),
            new BrowserEventElasticsearchBatchProcessor(client));
  }

  public static Properties retryQueueProducerProperties(String bootstrapServers) {
    Properties producerProperties = new Properties();
    producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerProperties.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProperties.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserEventSerializer.class.getName());
    return producerProperties;
  }

  public static Properties consumerProperties(String bootstrapServers) {
    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    consumerProps.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserEventDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    return consumerProps;
  }

  public void shutdown() {
    log.info("Shutting down ...");
    processor.shutdown();
  }

  public void start() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    log.info("Starting search indexer ...");
    processor.start();
  }
}
