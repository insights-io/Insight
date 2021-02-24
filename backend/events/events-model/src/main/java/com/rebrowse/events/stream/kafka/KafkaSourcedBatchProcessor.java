package com.rebrowse.events.stream.kafka;

import com.rebrowse.events.stream.processor.BatchProcessor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

@Slf4j
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class KafkaSourcedBatchProcessor<K, V> {

  private final RetryQueueStandaloneKafkaConsumer<K, V> consumer;
  private final BatchProcessor<V> processor;

  public KafkaSourcedBatchProcessor(
      RetryQueueStandaloneKafkaConsumer<K, V> consumer, BatchProcessor<V> processor) {
    this.consumer = Objects.requireNonNull(consumer);
    this.processor = Objects.requireNonNull(processor);
    this.processor.onFailure(this::handleFailures);
  }

  public void start() {
    log.info("Starting ...");
    try {
      startPolling();
    } catch (WakeupException ex) {
      log.error("Failed to wakeup consumer", ex);

    } catch (Exception ex) {
      log.error("Unexpected exception", ex);
    } finally {
      consumer.close();
      processor.close();
    }
  }

  private void startPolling() {
    while (true) {
      ConsumerRecords<K, V> records = consumer.poll();
      int numRecords = records.count();
      if (numRecords == 0) {
        log.debug("No records received during this poll");
        continue;
      }

      Collection<V> batchingFailures = new LinkedList<>();
      Set<TopicPartition> partitions = records.partitions();
      boolean isFirstRecordInPool = true;
      long poolStartMillis = 0L;

      for (ConsumerRecord<K, V> record : records) {
        int partition = record.partition();
        long offset = record.offset();
        V value = record.value();
        log.debug(
            "received record: partition: {}, offset: {}, value: {}", partition, offset, value);

        if (isFirstRecordInPool) {
          isFirstRecordInPool = false;
          log.debug("Start offset for partition {} in this poll : {}", partition, offset);
          poolStartMillis = System.currentTimeMillis();
        }

        try {
          processor.batch(value);
          log.debug("Successfully batched record {}", record);
        } catch (Exception ex) {
          log.error("Failed to batch record: {}", record, ex);
          batchingFailures.add(value);
        }
      }

      int numBatchingFailures = batchingFailures.size();
      long endOfPollLoopMillis = System.currentTimeMillis();
      long timeToProcessLoopMillis = endOfPollLoopMillis - poolStartMillis;

      log.info(
          "Last poll snapshot: numRecords: {}, numBatchingFailures: {}, timeToProcessLoop: {}ms, partitions: {}",
          numRecords,
          numBatchingFailures,
          timeToProcessLoopMillis,
          partitions);

      if (numBatchingFailures > 0) {
        log.info("Sending {} records to dead letter queue", numBatchingFailures);
        consumer.sendToDeadLetterQueue(batchingFailures);
      }

      consumer.commit(partitions);
    }
  }

  public void shutdown() {
    log.info("Shutting down ...");
    processor.shutdown();
    consumer.shutdown();
  }

  public void handleFailures(Collection<V> failures, Throwable cause) {
    log.info("Handling {} failures", failures.size(), cause);
    consumer.sendToRetryQueue(failures);
  }
}
