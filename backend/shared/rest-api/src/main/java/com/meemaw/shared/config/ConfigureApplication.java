package com.meemaw.shared.config;

import com.rebrowse.Rebrowse;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.NetHttpClient;
import com.rebrowse.net.RebrowseHttpClient;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class ConfigureApplication {

  @Inject ManagedExecutor managedExecutor;

  @ConfigProperty(name = "authorization.s2s.api.key")
  String s2sApiKey;

  void onStart(@Observes StartupEvent ev) {
    configureHttpClient();
  }

  private void configureHttpClient() {
    java.net.http.HttpClient.Builder builder = java.net.http.HttpClient.newBuilder();
    if (managedExecutor != null) {
      builder.executor(managedExecutor);
    }

    Rebrowse.apiKey = s2sApiKey;
    Rebrowse.maxNetworkRetries(2);
    ApiResource.setHttpClient(
        new RebrowseHttpClient(new NetHttpClient(builder.build()), ApiResource.OBJECT_MAPPER));
  }
}
