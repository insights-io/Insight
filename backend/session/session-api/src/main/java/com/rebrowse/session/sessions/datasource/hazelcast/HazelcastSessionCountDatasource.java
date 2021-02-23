package com.rebrowse.session.sessions.datasource.hazelcast;

import com.rebrowse.session.sessions.datasource.SessionCountDatasource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;

// TODO: this should be moved to billing api and actually use Hazelcast
// TODO: using in memory implementation for now as there are issues with classloading
// TODO: when multiple hazelcast instances are started on same machine (locally)
@ApplicationScoped
public class HazelcastSessionCountDatasource implements SessionCountDatasource {

  private final Map<String, Long> countMap = new HashMap<>();

  @Override
  public CompletionStage<Long> incrementAndGet(String key) {
    long currentValue = countMap.getOrDefault(key, 0L);
    long updatedValue = currentValue + 1;
    countMap.put(key, updatedValue);
    return CompletableFuture.completedStage(updatedValue);
  }
}
