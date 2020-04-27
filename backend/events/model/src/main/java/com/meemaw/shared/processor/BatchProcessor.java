package com.meemaw.shared.processor;

public interface BatchProcessor<V> {

  void batch(V value);

  void onFailure(BatchProcessorFailureCallback<V> callback);

  void shutdown();

  void close();

  void flush();

}
