package com.meemaw.shared.kafka;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class RetryQueueStandaloneKafkaConsumer<K, V> extends StandaloneKafkaConsumer<K, V> {

  private final KafkaProducer<K, V> producer;
  private final String retryQueue;
  private final String deadLetterQueue;

  public RetryQueueStandaloneKafkaConsumer(
      Properties consumerProperties,
      Properties producerProperties,
      String topic,
      String retryQueue,
      String deadLetterQueue) {
    super(new KafkaConsumer<>(Objects.requireNonNull(consumerProperties)),
        new StandaloneKafkaCommitCallback());
    subscribe(Collections.singletonList(topic));
    this.producer = new KafkaProducer<>(Objects.requireNonNull(producerProperties));
    this.retryQueue = Objects.requireNonNull(retryQueue);
    this.deadLetterQueue = Objects.requireNonNull(deadLetterQueue);
  }

  public Future<RecordMetadata> sendToRetryQueue(V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(retryQueue, value);
    return producer.send(record, (metadata, ex) -> {
      if (ex != null) {
        log.error("Failed to send record {} to retry queue {}", record, retryQueue, ex);
      } else {
        log.info("Written record {} to retry queue {}", record, retryQueue);
      }
    });
  }

  public Future<RecordMetadata> sendToDeadLetterQueue(V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(deadLetterQueue, value);
    return producer.send(record, (metadata, ex) -> {
      if (ex != null) {
        log.error("Failed to send record {} to dead letter queue {}", record, deadLetterQueue, ex);
      } else {
        log.info("Written record {} to dead letter queue {}", record, deadLetterQueue);
      }
    });
  }

  public void sendToRetryQueue(Collection<V> values) {
    values.forEach(this::sendToRetryQueue);
  }

  public void sendToDeadLetterQueue(Collection<V> values) {
    values.forEach(this::sendToDeadLetterQueue);
  }

}
