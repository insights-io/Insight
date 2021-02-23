package com.rebrowse.events.search.indexer;

import static org.awaitility.Awaitility.with;

import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.model.incoming.UserEvent;
import com.rebrowse.test.testconainers.kafka.Kafka;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@Kafka
@Slf4j
public class SearchIndexerDeadLetterQueueTest extends AbstractSearchIndexerTest {

  @AfterEach
  public void cleanup() {
    searchIndexers.forEach(SearchIndexer::shutdown);
  }

  @Test
  public void shouldWriteToDlqAfterRetryQuotaExceeded() {
    // Configure Kafka
    KafkaProducer<String, UserEvent<AbstractBrowserEvent<?>>> producer = configureProducer();

    int numRecords = 5;
    for (int i = 0; i < numRecords; i++) {
      producer.send(new ProducerRecord<>(SOURCE_TOPIC_NAME, null));
    }

    // Configure ElasticSearch: doesn't really matter as we wont send to ElasticSearch
    RestHighLevelClient client =
        new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 10000, "http")));

    spawnIndexer(client);

    // Configure DQL consumer
    KafkaConsumer<String, UserEvent<AbstractBrowserEvent<?>>> deadLetterQueueConsumer =
        deadLetterQueueConsumer();

    AtomicInteger numConsumedDeadLetterQueueEvents = new AtomicInteger(0);

    with()
        .atMost(30, TimeUnit.SECONDS)
        .until(
            () -> {
              ConsumerRecords<String, UserEvent<AbstractBrowserEvent<?>>> records =
                  deadLetterQueueConsumer.poll(Duration.ofMillis(1000));
              int count = numConsumedDeadLetterQueueEvents.addAndGet(records.count());
              log.info("Num events in dead letter queue: {}", count);
              return count == numRecords;
            });

    producer.close();
    deadLetterQueueConsumer.close();
  }
}
