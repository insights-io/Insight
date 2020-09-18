package com.meemaw.session.datasource.hazelcast;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.runtime.ShutdownEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class HazelcastProvider {

  private HazelcastInstance instance;

  public HazelcastInstance getInstance() {
    if (instance == null) {
      log.info("Initializing HazelcastInstance...");
      instance = Hazelcast.newHazelcastInstance(new XmlConfigBuilder().build());
    }
    return instance;
  }

  public void shutdown(@Observes ShutdownEvent event) {
    if (instance != null) {
      log.info("Shutting down HazelcastInstance...");
      instance.shutdown();
    }
  }
}
