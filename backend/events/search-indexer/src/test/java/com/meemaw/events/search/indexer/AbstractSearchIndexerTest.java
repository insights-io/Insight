package com.meemaw.events.search.indexer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.external.serialization.UserEventSerializer;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import com.meemaw.test.rest.mappers.JacksonMapper;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.meemaw.test.testconainers.kafka.KafkaTestExtension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
public class AbstractSearchIndexerTest {

  protected final List<SearchIndexer> searchIndexers = new LinkedList<>();

  protected static final String SOURCE_TOPIC_NAME = "test-events";
  protected static final String RETRY_TOPIC_NAME = "test-events-0";
  protected static final String DEAD_LETTER_TOPIC_NAME = "test-events-dql";

  protected static final SearchRequest SEARCH_REQUEST =
      new SearchRequest().indices(EventIndex.NAME);

  protected SearchIndexer spawnIndexer(RestHighLevelClient client) {
    SearchIndexer searchIndexer =
        new SearchIndexer(
            SOURCE_TOPIC_NAME,
            RETRY_TOPIC_NAME,
            DEAD_LETTER_TOPIC_NAME,
            KafkaTestExtension.getInstance().getBootstrapServers(),
            client);
    CompletableFuture.runAsync(searchIndexer::start);
    searchIndexers.add(searchIndexer);
    return searchIndexer;
  }

  protected SearchIndexer spawnIndexer(HttpHost... hosts) {
    return spawnIndexer(new RestHighLevelClient(RestClient.builder(hosts)));
  }

  protected SearchIndexer spawnIndexer() {
    return spawnIndexer(ElasticsearchTestExtension.getInstance().getHttpHost());
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
                    SOURCE_TOPIC_NAME, event))
        .collect(Collectors.toList());
  }

  protected Collection<ProducerRecord<String, UserEvent<AbstractBrowserEvent>>> readKafkaRecords(
      String path) throws IOException, URISyntaxException {
    String payload =
        Files.readString(
            Path.of(
                Objects.requireNonNull(
                        Thread.currentThread().getContextClassLoader().getResource(path))
                    .toURI()));

    Collection<UserEvent<AbstractBrowserEvent>> batch =
        JacksonMapper.get().readValue(payload, new TypeReference<>() {});

    return kafkaRecords(batch);
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
    Properties properties =
        SearchIndexer.consumerProperties(KafkaTestExtension.getInstance().getBootstrapServers());
    KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> consumer =
        new KafkaConsumer<>(properties);
    consumer.subscribe(Collections.singletonList(topicName));
    return consumer;
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> retryQueueConsumer() {
    return eventsConsumer(RETRY_TOPIC_NAME);
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent>> deadLetterQueueConsumer() {
    return eventsConsumer(DEAD_LETTER_TOPIC_NAME);
  }
}
