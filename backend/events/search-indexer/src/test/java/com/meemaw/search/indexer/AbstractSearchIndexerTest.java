package com.meemaw.search.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meemaw.shared.event.EventsChannel;
import com.meemaw.shared.kafka.event.serialization.BrowserEventSerializer;
import com.meemaw.shared.event.model.AbstractBrowserEvent;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchExtension;
import com.meemaw.test.testconainers.kafka.KafkaExtension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

public abstract class AbstractSearchIndexerTest {

  protected static final String RETRY_QUEUE = "retry-queue-0";
  protected static final String DEAD_LETTER_QUEUE = "dead-letter-queue";

  protected static final SearchRequest SEARCH_REQUEST = new SearchRequest()
      .indices(EventIndex.NAME);

  protected void spawnIndexer(String bootstrapServers, RestHighLevelClient client) {
    CompletableFuture.runAsync(() -> {
      SearchIndexer searchIndexer = new SearchIndexer(
          RETRY_QUEUE,
          DEAD_LETTER_QUEUE,
          bootstrapServers,
          client
      );
      searchIndexer.start();
    });
  }

  protected void spawnIndexer(String bootstrapServers, HttpHost... hosts) {
    spawnIndexer(bootstrapServers, new RestHighLevelClient(RestClient.builder(hosts)));
  }

  protected void spawnIndexer() {
    spawnIndexer(KafkaExtension.getInstance().getBootstrapServers(),
        ElasticsearchExtension.getInstance().getHttpHost());
  }

  protected KafkaProducer<String, AbstractBrowserEvent> configureProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        KafkaExtension.getInstance().getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BrowserEventSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  protected Collection<ProducerRecord<String, AbstractBrowserEvent>> kafkaRecords(
      Collection<AbstractBrowserEvent> batch) {
    return batch.stream()
        .map(event -> new ProducerRecord<String, AbstractBrowserEvent>(EventsChannel.ALL, event))
        .collect(Collectors.toList());
  }

  protected Collection<ProducerRecord<String, AbstractBrowserEvent>> readKafkaRecords(String path)
      throws IOException, URISyntaxException {
    String payload = Files
        .readString(Path.of(getClass().getClassLoader().getResource(path).toURI()));

    Collection<AbstractBrowserEvent> batch = JacksonMapper.get()
        .readValue(payload, new TypeReference<>() {
        });

    return kafkaRecords(batch);
  }

  protected String bootstrapServers() {
    return KafkaExtension.getInstance().getBootstrapServers();
  }

  protected CreateIndexResponse createIndex(RestHighLevelClient client) throws IOException {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(EventIndex.NAME);
    return client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
  }

  protected Collection<Future<RecordMetadata>> writeSmallBatch(
      KafkaProducer<String, AbstractBrowserEvent> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/small.json")
        .stream()
        .map(record -> producer.send(record, ((metadata, exception) -> {
          if (exception != null) {
            throw new RuntimeException(exception);
          }
          System.out
              .println(String
                  .format("Wrote small batch to topic=%s, partition=%d, offset=%d",
                      metadata.topic(), metadata.partition(), metadata.offset()));
        }))).collect(Collectors.toList());
  }

  protected Collection<Future<RecordMetadata>> writeLargeBatch(
      KafkaProducer<String, AbstractBrowserEvent> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/large.json")
        .stream()
        .map(record -> producer.send(record, (meta, exception) -> {
          if (exception != null) {
            throw new RuntimeException(exception);
          }
          System.out
              .println(String
                  .format("Wrote large batch to topic=%s, partition=%d, offset=%d", meta.topic(),
                      meta.partition(), meta.offset()));
        })).collect(Collectors.toList());
  }

  private KafkaConsumer<String, AbstractBrowserEvent> eventsConsumer(String topicName) {
    Properties properties = SearchIndexer.consumerProperties(bootstrapServers());
    KafkaConsumer<String, AbstractBrowserEvent> consumer = new KafkaConsumer<>(properties);
    consumer.subscribe(Collections.singletonList(topicName));
    return consumer;
  }

  protected KafkaConsumer<String, AbstractBrowserEvent> retryQueueConsumer() {
    return eventsConsumer(RETRY_QUEUE);
  }

  protected KafkaConsumer<String, AbstractBrowserEvent> deadLetterQueueConsumer() {
    return eventsConsumer(DEAD_LETTER_QUEUE);
  }
}
