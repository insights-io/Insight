package com.meemaw.auth.sso.datasource;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HazelcastProvider {

  private static HazelcastInstance hazelcastInstance;

  public HazelcastInstance getInstance() {
    if (hazelcastInstance == null) {
      Config hazConfig = new XmlConfigBuilder().build();
      hazelcastInstance = Hazelcast.newHazelcastInstance(hazConfig);
    }
    return hazelcastInstance;
  }

}
