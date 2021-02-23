package com.rebrowse.events.search.indexer;

import static com.rebrowse.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.events.index.UserEventIndex;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.model.incoming.UserEvent;
import com.rebrowse.events.model.incoming.serialization.UserEventSerializer;
import com.rebrowse.test.rest.data.EventTestData;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import com.rebrowse.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.rebrowse.test.testconainers.kafka.KafkaTestExtension;
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
import java.util.UUID;
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

  protected static final int LARGE_BATCH_SIZE = 384;
  protected static final int SMALL_BATCH_SIZE = 1;

  protected static final String SOURCE_TOPIC_NAME = "test-events";
  protected static final String RETRY_TOPIC_NAME = "test-events-0";
  protected static final String DEAD_LETTER_TOPIC_NAME = "test-events-dql";

  protected static final SearchRequest SEARCH_REQUEST =
      new SearchRequest().indices(UserEventIndex.NAME);

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

  protected KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> configureProducer() {
    Properties props = new Properties();
    props.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        KafkaTestExtension.getInstance().getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserEventSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  protected Collection<ProducerRecord<String, UserEvent<AbstractBrowserEvent<?>>>> kafkaRecords(
      Collection<UserEvent<AbstractBrowserEvent<?>>> batch) {
    return batch.stream()
        .map(
            event ->
                new ProducerRecord<String, UserEvent<AbstractBrowserEvent<?>>>(
                    SOURCE_TOPIC_NAME, event))
        .collect(Collectors.toList());
  }

  protected Collection<ProducerRecord<String, UserEvent<AbstractBrowserEvent<?>>>> readKafkaRecords(
      String path) throws IOException, URISyntaxException {
    String payload =
        Files.readString(
            Path.of(
                Objects.requireNonNull(
                        Thread.currentThread().getContextClassLoader().getResource(path))
                    .toURI()));

    Collection<UserEvent<AbstractBrowserEvent<?>>> batch =
        JacksonMapper.get().readValue(payload, new TypeReference<>() {});

    return kafkaRecords(batch);
  }

  protected CreateIndexResponse createIndex(RestHighLevelClient client) throws IOException {
    CreateIndexRequest createIndexRequest =
        new CreateIndexRequest(UserEventIndex.NAME).mapping(UserEventIndex.MAPPING);
    return client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
  }

  protected Collection<Future<RecordMetadata>> writeSmallBatch(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/small.json").stream()
        .map(record -> writeEvent(producer, record))
        .collect(Collectors.toList());
  }

  private Future<RecordMetadata> writeEvent(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer,
      UserEvent<AbstractBrowserEvent<?>> event) {
    return writeEvent(producer, new ProducerRecord<>(SOURCE_TOPIC_NAME, event));
  }

  private Future<RecordMetadata> writeEvent(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer,
      ProducerRecord<String, UserEvent<AbstractBrowserEvent<?>>> record) {

    return producer.send(
        record,
        ((metadata, exception) -> {
          if (exception != null) {
            throw new RuntimeException(exception);
          }
          log.info(
              "Wrote record to topic={}, partition={}, offset={}",
              metadata.topic(),
              metadata.partition(),
              metadata.offset());
        }));
  }

  protected Collection<Future<RecordMetadata>> writeLargeBatch(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer)
      throws IOException, URISyntaxException {
    return readKafkaRecords("eventsBatch/large.json").stream()
        .map(record -> writeEvent(producer, record))
        .collect(Collectors.toList());
  }

  private KafkaConsumer<String, UserEvent<AbstractBrowserEvent<?>>> eventsConsumer(
      String topicName) {
    Properties properties =
        SearchIndexer.consumerProperties(KafkaTestExtension.getInstance().getBootstrapServers());
    KafkaConsumer<String, UserEvent<AbstractBrowserEvent<?>>> consumer =
        new KafkaConsumer<>(properties);
    consumer.subscribe(Collections.singletonList(topicName));
    return consumer;
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent<?>>> retryQueueConsumer() {
    return eventsConsumer(RETRY_TOPIC_NAME);
  }

  protected KafkaConsumer<String, UserEvent<AbstractBrowserEvent<?>>> deadLetterQueueConsumer() {
    return eventsConsumer(DEAD_LETTER_TOPIC_NAME);
  }

  @SuppressWarnings("rawtypes")
  private Collection<AbstractBrowserEvent> loadIncomingEvents()
      throws URISyntaxException, IOException {
    return EventTestData.readIncomingEvents().stream()
        .map(
            eventPayload -> {
              try {
                return JacksonMapper.get().readValue(eventPayload, AbstractBrowserEvent.class);
              } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
              }
            })
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected Collection<Future<RecordMetadata>> indexIncomingEvents(
      KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer)
      throws IOException, URISyntaxException {
    return loadIncomingEvents().stream()
        .map(
            browserEvent ->
                writeEvent(
                    producer,
                    new UserEvent<>(
                        browserEvent,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        REBROWSE_ORGANIZATION_ID)))
        .collect(Collectors.toList());
  }
}
