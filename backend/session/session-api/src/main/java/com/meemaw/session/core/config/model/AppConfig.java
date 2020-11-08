package com.meemaw.session.core.config.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpHost;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.meemaw.shared.config.model.AppConfigBase;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends AppConfigBase {

  HttpHost[] elasticsearchHttpHost = ElasticsearchUtils.httpHosts();

  @ConfigProperty(name = "quarkus.datasource.reactive.url")
  String datasourceURL;

  @ConfigProperty(name = "kafka.bootstrap.servers")
  String kafkaBootstrapServers;

  @ConfigProperty(name = "auth-api/mp-rest/url")
  String authApiBaseURL;
}
