package com.rebrowse.shared.hazelcast.processors;

import com.hazelcast.map.EntryProcessor;
import java.util.Map.Entry;

public class IncrementCounterEntryProcessor implements EntryProcessor<String, Long, Long> {

  @Override
  public Long process(Entry<String, Long> entry) {
    Long currentValue = entry.getValue();
    if (currentValue == null) {
      currentValue = 0L;
    }
    long updatedValue = currentValue + 1;
    entry.setValue(updatedValue);
    return updatedValue;
  }
}
