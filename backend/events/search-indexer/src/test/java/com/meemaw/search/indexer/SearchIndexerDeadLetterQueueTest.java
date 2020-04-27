package com.meemaw.search.indexer;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.junit.jupiter.api.Assertions.assertEquals;


import com.meemaw.shared.event.EventsChannel;
import com.meemaw.shared.event.model.AbstractBrowserEvent;
import com.meemaw.test.testconainers.kafka.Kafka;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;

@Kafka
public class SearchIndexerDeadLetterQueueTest extends AbstractSearchIndexerTest {


  @Test
  public void shouldWriteToDlqAfterRetryQuotaExceeded() throws InterruptedException {
    // Configure Kafka
    KafkaProducer<String, AbstractBrowserEvent> producer = configureProducer();

    int numRecords = 5;
    for (int i = 0; i < numRecords; i++) {
      producer.send(new ProducerRecord<>(EventsChannel.ALL, null));
    }

    // Configure ElasticSearch: doesn't really matter as we wont send to ElasticSearch
    RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(new HttpHost("localhost", 10000, "http")));

    spawnIndexer(bootstrapServers(), client);

    // Configure DQL consumer
    KafkaConsumer<String, AbstractBrowserEvent> deadLetterQueueConsumer = deadLetterQueueConsumer();

    AtomicInteger numConsumedDeadLetterQueueEvents = new AtomicInteger(0);

    with().atMost(10, TimeUnit.SECONDS).until(() -> {
      ConsumerRecords<String, AbstractBrowserEvent> records = deadLetterQueueConsumer
          .poll(Duration.ofMillis(1000));
      int count = numConsumedDeadLetterQueueEvents.addAndGet(records.count());
      return count == numRecords;
    });

  }
}
