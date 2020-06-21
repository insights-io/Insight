package com.meemaw.session.core.config.model;

import javax.enterprise.context.ApplicationScoped;
import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Data
public class AppConfig {

  @ConfigProperty(name = "quarkus.datasource.url")
  String datasourceURL;

  @ConfigProperty(name = "git.commit.sha")
  String gitCommitSha;

  @ConfigProperty(name = "kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @ConfigProperty(name = "sso-resource/mp-rest/url")
  String ssoResourceBaseURL;
}
