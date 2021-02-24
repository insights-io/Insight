package com.rebrowse.events.stream.kafka;

import com.rebrowse.events.stream.consumer.StandaloneConsumer;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

@Slf4j
public class StandaloneKafkaConsumer<K, V>
    implements StandaloneConsumer<
        ConsumerRecords<K, V>, Set<TopicPartition>, Map<TopicPartition, OffsetAndMetadata>> {

  private final KafkaConsumer<K, V> consumer;
  private final StandaloneKafkaCommitCallback commitCallback;

  public StandaloneKafkaConsumer(
      KafkaConsumer<K, V> consumer, StandaloneKafkaCommitCallback commitCallback) {
    this.consumer = Objects.requireNonNull(consumer);
    this.commitCallback = Objects.requireNonNull(commitCallback);
  }

  public void subscribe(Collection<String> topics) {
    this.consumer.subscribe(topics, commitCallback);
  }

  @Override
  public ConsumerRecords<K, V> poll(Duration duration) {
    return consumer.poll(duration);
  }

  @Override
  public void commit(Set<TopicPartition> partitions) {
    Map<TopicPartition, OffsetAndMetadata> offsets = getOffsets(partitions);
    log.info("Committing offsets {}", offsets);
    consumer.commitAsync(offsets, commitCallback);
  }

  @Override
  public Map<TopicPartition, OffsetAndMetadata> getOffsets(Set<TopicPartition> partitions) {
    Map<TopicPartition, OffsetAndMetadata> nextCommitableOffset = new HashMap<>(partitions.size());
    for (TopicPartition topicPartition : partitions) {
      long topicPosition = consumer.position(topicPartition);
      nextCommitableOffset.put(topicPartition, new OffsetAndMetadata(topicPosition));
    }
    return nextCommitableOffset;
  }

  @Override
  public void shutdown() {
    log.info("Shutting down ...");
    commitCallback
        .getPartitionOffsetMap()
        .forEach(
            (topicPartition, offset) ->
                log.info(
                    "Offset position during the shutdown: partition : {}, offset : {}",
                    topicPartition.partition(),
                    offset.offset()));
    consumer.wakeup();
  }

  @Override
  public void close() {
    log.info("Closing ...");
    consumer.close();
  }
}
