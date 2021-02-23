package com.rebrowse.events.stream.consumer;

import java.time.Duration;

public interface StandaloneConsumer<R, P, O> {

  Duration ONE_SECOND = Duration.ofSeconds(1);

  default R poll() {
    return poll(ONE_SECOND);
  }

  R poll(Duration duration);

  void commit(P partitions);

  O getOffsets(P partitions);

  void shutdown();

  void close();
}
