package com.meemaw.auth.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.enterprise.context.ApplicationScoped;
import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Data
public class Config {

  @ConfigProperty(name = "quarkus.datasource.url")
  String datasourceURL;

  @ConfigProperty(name = "google.oauth.client.id")
  String googleOAuthClientId;

  @ConfigProperty(name = "git.commit.sha")
  String gitCommitSha;

  @JsonIgnore
  @ConfigProperty(name = "google.oauth.client.secret")
  String googleOAuthClientSecret;
}
