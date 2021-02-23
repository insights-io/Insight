package com.rebrowse.events.stream.kafka;

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
  private final String retryTopicName;
  private final String deadLetterTopicName;

  public RetryQueueStandaloneKafkaConsumer(
      Properties consumerProperties,
      Properties producerProperties,
      String sourceTopicName,
      String retryTopicName,
      String deadLetterTopicName) {
    super(
        new KafkaConsumer<>(Objects.requireNonNull(consumerProperties)),
        new StandaloneKafkaCommitCallback());
    subscribe(Collections.singletonList(sourceTopicName));
    this.producer = new KafkaProducer<>(Objects.requireNonNull(producerProperties));
    this.retryTopicName = Objects.requireNonNull(retryTopicName);
    this.deadLetterTopicName = Objects.requireNonNull(deadLetterTopicName);
  }

  public Future<RecordMetadata> sendToRetryQueue(V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(retryTopicName, value);
    return producer.send(
        record,
        (metadata, ex) -> {
          if (ex == null) {
            log.info("Written record {} to retry topic {}", record, retryTopicName);
          } else {
            log.error("Failed to send record {} to retry topic {}", record, retryTopicName, ex);
          }
        });
  }

  public Future<RecordMetadata> sendToDeadLetterQueue(V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(deadLetterTopicName, value);
    return producer.send(
        record,
        (metadata, ex) -> {
          if (ex == null) {
            log.info("Written record {} to dead letter queue {}", record, deadLetterTopicName);
          } else {
            log.error(
                "Failed to send record {} to dead letter queue {}",
                record,
                deadLetterTopicName,
                ex);
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
