package com.rebrowse.events.stream.kafka;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;

@Slf4j
public class StandaloneKafkaCommitCallback
    implements OffsetCommitCallback, ConsumerRebalanceListener {

  private final Map<TopicPartition, OffsetAndMetadata> partitionOffsetMap;

  public StandaloneKafkaCommitCallback() {
    this.partitionOffsetMap = new ConcurrentHashMap<>();
  }

  @Override
  public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
    String thread = Thread.currentThread().getName();
    if (exception == null) {
      offsets.forEach(
          (topicPartition, offsetAndMetadata) -> {
            partitionOffsetMap.computeIfPresent(topicPartition, (k, v) -> offsetAndMetadata);
            log.info(
                "onComplete(): offset position during commit for consumerId : {}, partition : {}, offset : {}",
                thread,
                topicPartition.partition(),
                offsetAndMetadata.offset());
          });
    } else {
      offsets.forEach(
          (topicPartition, offsetAndMetadata) ->
              log.error(
                  "onComplete(): offset position during commit when exception != null:  error: {}, "
                      + "consumerId : {}, partition : {}, offset : {}",
                  exception.getMessage(),
                  thread,
                  topicPartition.partition(),
                  offsetAndMetadata.offset(),
                  exception));
    }
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    log.info("Partitions revoked : {}", partitions);
    for (TopicPartition currentPartition : partitions) {
      partitionOffsetMap.remove(currentPartition);
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    log.info("Partitions assigned : {}", partitions);
    for (TopicPartition currentPartition : partitions) {
      partitionOffsetMap.put(currentPartition, new OffsetAndMetadata(0L, "Initial default offset"));
    }
  }

  public Map<TopicPartition, OffsetAndMetadata> getPartitionOffsetMap() {
    return partitionOffsetMap;
  }
}
