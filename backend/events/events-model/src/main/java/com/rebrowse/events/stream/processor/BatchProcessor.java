package com.rebrowse.events.stream.processor;

public interface BatchProcessor<V> {

  void batch(V value);

  void onFailure(BatchProcessorFailureCallback<V> callback);

  void shutdown();

  void close();

  void flush();
}
