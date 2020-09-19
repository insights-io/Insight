package com.meemaw.session.sessions.datasource.hazelcast;

import com.hazelcast.map.EntryProcessor;
import java.util.Map.Entry;
import java.util.Optional;

public class IncrementCountEntryProcessor implements EntryProcessor<String, Long, Long> {

  @Override
  public Long process(Entry<String, Long> entry) {
    long currentValue = Optional.ofNullable(entry.getValue()).orElse(0L);
    long updatedValue = currentValue + 1;
    entry.setValue(updatedValue);
    return updatedValue;
  }
}
