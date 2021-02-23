package com.rebrowse.auth.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebrowse.shared.config.model.AppConfigBase;
import javax.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends AppConfigBase {

  @ConfigProperty(name = "quarkus.datasource.reactive.url")
  String datasourceURL;

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOpenIdClientId;

  @JsonIgnore
  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOpenIdClientSecret;

  @ConfigProperty(name = "github.oauth.client.id")
  String githubOpenIdClientId;

  @JsonIgnore
  @ConfigProperty(name = "github.oauth.client.secret")
  String githubOpenIdClientSecret;

  @ConfigProperty(name = "microsoft.oauth.client.id")
  String microsoftOpenIdClientId;

  @JsonIgnore
  @ConfigProperty(name = "microsoft.oauth.client.secret")
  String microsoftOpenIdClientSecret;
}
