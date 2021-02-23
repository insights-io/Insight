package com.rebrowse.shared.config;

import com.rebrowse.Rebrowse;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
