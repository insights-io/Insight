package com.meemaw.auth.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meemaw.shared.config.model.AppConfigBase;
import javax.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Data
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends AppConfigBase {

  @ConfigProperty(name = "quarkus.datasource.url")
  String datasourceURL;

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOAuthClientId;

  @JsonIgnore
  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOAuthClientSecret;

  @ConfigProperty(name = "github.oauth.client.id")
  String githubOAuthClientId;

  @ConfigProperty(name = "github.oauth.client.secret")
  String githubOAuthClientSecret;
}
