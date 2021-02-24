package com.rebrowse.events.stream.processor;

import java.util.Collection;

@FunctionalInterface
public interface BatchProcessorFailureCallback<V> {

  void execute(Collection<V> failures, Throwable cause);
}
