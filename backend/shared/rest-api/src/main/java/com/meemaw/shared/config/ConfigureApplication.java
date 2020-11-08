package com.meemaw.shared.config;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.rebrowse.Rebrowse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class ConfigureApplication {

  @ConfigProperty(name = "authorization.s2s.api.key")
  String s2sApiKey;

  void onStart(@Observes StartupEvent ev) {
    configureHttpClient();
  }

  private void configureHttpClient() {
    Rebrowse.apiKey = s2sApiKey;
    Rebrowse.maxNetworkRetries(2);
  }
}
