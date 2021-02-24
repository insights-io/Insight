package com.rebrowse.shared.hazelcast.processors;

import com.hazelcast.map.EntryProcessor;
import java.util.Map.Entry;

public class SetValueEntryProcessor<K, V, R> implements EntryProcessor<K, V, R> {

  private final V value;

  public SetValueEntryProcessor(V value) {
    this.value = value;
  }

  @Override
  public R process(Entry<K, V> entry) {
    entry.setValue(value);
    return null;
  }
}
