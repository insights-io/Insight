package com.rebrowse.shared.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

@ApplicationScoped
public class MetricsService {

  @Inject
  @RegistryType(type = MetricRegistry.Type.APPLICATION)
  MetricRegistry appRegistry;

  public Counter requestCount() {
    return appRegistry.counter("requests_total");
  }

  public Histogram requestDuration(Tag... tags) {
    return appRegistry.histogram("request_duration_ms", tags);
  }

  public Histogram requestDuration(String method, String path, int status) {
    return requestDuration(path(path), method(method), status(status));
  }

  public Counter requestError(int status) {
    return appRegistry.counter("request_errors_total", status(status));
  }

  public Counter requestClientError(int status) {
    return appRegistry.counter("request_client_errors_total", status(status));
  }

  public Counter requestServerError(int status) {
    return appRegistry.counter("request_server_errors_total", status(status));
  }

  private Tag path(String endpoint) {
    return new Tag("path", endpoint);
  }

  private Tag status(int status) {
    return new Tag("status", String.valueOf(status));
  }

  private Tag method(String method) {
    return new Tag("method", method);
  }
}
