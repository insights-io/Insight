package com.meemaw.auth.sso.datasource;

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

  /**
   * Lazy instantiate hazelcast instance for faster startup.
   *
   * @return hazelcast instance
   */
  public HazelcastInstance getInstance() {
    if (instance == null) {
      instance = Hazelcast.newHazelcastInstance(new XmlConfigBuilder().build());
    }
    return instance;
  }

  /**
   * Gracefully terminate hazelcast instance.
   *
   * @param event shutdown event
   */
  public void shutdown(@Observes ShutdownEvent event) {
    log.info("Shutting down ...");
    if (instance != null) {
      instance.shutdown();
    }
  }
}
