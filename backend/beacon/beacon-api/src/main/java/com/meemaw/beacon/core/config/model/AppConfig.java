package com.meemaw.beacon.core.config.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.meemaw.shared.config.model.AppConfigBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends AppConfigBase {

  @ConfigProperty(name = "kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @ConfigProperty(name = "session-api/mp-rest/url")
  String sessionResourceBaseURL;
}
