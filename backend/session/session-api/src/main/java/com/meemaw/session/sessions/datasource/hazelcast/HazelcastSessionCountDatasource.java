package com.meemaw.session.sessions.datasource.hazelcast;

import com.hazelcast.map.IMap;
import com.meemaw.session.datasource.hazelcast.HazelcastProvider;
import com.meemaw.session.sessions.datasource.SessionCountDatasource;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HazelcastSessionCountDatasource implements SessionCountDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  private IMap<String, Long> countMap;

  @ConfigProperty(name = "hazelcast.session.count-map")
  String sessionCountMapName;

  @PostConstruct
  public void init() {
    countMap = hazelcastProvider.getInstance().getMap(sessionCountMapName);
  }

  @Override
  public CompletionStage<Long> incrementAndGet(String key) {
    return countMap.submitToKey(key, new IncrementCountEntryProcessor());
  }
}
