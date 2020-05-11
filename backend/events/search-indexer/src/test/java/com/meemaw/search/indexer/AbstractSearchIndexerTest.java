package com.meemaw.search.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.external.serialization.UserEventSerializer;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.events.stream.EventsStream;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.meemaw.test.testconainers.kafka.KafkaTestExtension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public abstract class AbstractSearchIndexerTest {

  protected static final String RETRY_QUEUE = "retry-queue-0";
  protected static final String DEAD_LETTER_QUEUE = "dead-letter-queue";

  protected static final SearchRequest SEARCH_REQUEST =
      new SearchRequest().indices(EventIndex.NAME);

  protected SearchIndexer spawnIndexer(String bootstrapServers, RestHighLevelClient client) {
    SearchIndexer searchIndexer =
        new SearchIndexer(RETRY_QUEUE, DEAD_LETTER_QUEUE, bootstrapServers, client);
    CompletableFuture.runAsync(searchIndexer::start);
    return searchIndexer;
  }

  protected SearchIndexer spawnIndexer(String bootstrapServers, HttpHost... hosts) {
    return spawnIndexer(bootstrapServers, new RestHighLevelClient(RestClient.builder(hosts)));
  }

  protected SearchIndexer spawnIndexer() {
    return spawnIndexer(
        KafkaTestExtension.getInstance().getBootstrapServers(),
        ElasticsearchTestExtension.getInstance().getHttpHost());
  }

  protected KafkaProducer<String, UserEvent<AbstractBrowserEvent>> configureProducer() {
    Properties props = new Properties();
    props.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        KafkaTestExtension.getInstance().getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserEventSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  protected Collection<ProducerRecord<String, UserEvent<AbstractBrowserEvent>>> kafkaRecords(
      Collection<UserEvent<AbstractBrowserEvent>> batch) {
    return batch.stream()
        .map(
            event ->
                new ProducerRecord<String, UserEvent<AbstractBrowserEvent>>(
                    EventsStream.ALL, event))
        .collect(Collectors.toList());
  }

  protected Collection<ProducerRecord<String, UserEvent<AbstractBrowserEvent>>> readKafkaRecords(
      String path) throws IOException, URISyntaxException {
    String payload =
        Files.readString(
            Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource(path)).toURI()));

    Collection<UserEvent<AbstractBrowserEvent>> batch =
        JacksonMapper.get().readValue(payload, new TypeReference<>() {});

    return kafkaRecords(batch);
  }

  protected String bootstrapServers() {
    return KafkaTestExtension.getInstance().getBootstrapServers();
  }

  protected CreateIndexResponse createIndex(RestHighLevelClient client) throws IOException {
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(EventIndex.NAME);
    return client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
  }

  protected Collection<Future<RecordMetadata>> writeSmallBatch(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent>> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/small.json").stream()
        .map(
            record ->
                producer.send(
                    record,
                    ((metadata, exception) -> {
                      if (exception != null) {
                        throw new RuntimeException(exception);
                      }
                      log.info(
                          "Wrote small batch to topic={}, partition={}, offset={}",
                          metadata.topic(),
                          metadata.partition(),
                          metadata.offset());
                    })))
        .collect(Collectors.toList());
  }

  protected Collection<Future<RecordMetadata>> writeLargeBatch(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent>> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/large.json").stream()
        .map(
            record ->
                producer.send(
                    record,
                    (meta, exception) -> {
                      if (exception != null) {
                        throw new RuntimeException(exception);
                      }
                      log.info(
                          "Wrote large batch to topic={}, partition={}, offset={}",
                          meta.topic(),
                          meta.partition(),
                          meta.offset());
                    }))
        .collect(Collectors.toList());
  }

  private KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> eventsConsumer(String topicName) {
    Properties properties = SearchIndexer.consumerProperties(bootstrapServers());
    KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> consumer =
        new KafkaConsumer<>(properties);
    consumer.subscribe(Collections.singletonList(topicName));
    return consumer;
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> retryQueueConsumer() {
    return eventsConsumer(RETRY_QUEUE);
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> deadLetterQueueConsumer() {
    return eventsConsumer(DEAD_LETTER_QUEUE);
  }
}
