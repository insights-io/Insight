package com.meemaw.shared.rest.filter;

import static javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;

import com.meemaw.shared.metrics.MetricsService;
import io.vertx.core.http.HttpServerRequest;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@Provider
@Priority(0)
@Slf4j
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";

  private static final String LOG_START_TIME_PROPERTY = "X-Start-Time";
  private static final long REQUEST_LATENCY_LOG_LIMIT_MS = 200;

  @Inject MetricsService metricsService;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  /**
   * Generate a unique request id.
   *
   * @return unique request id
   */
  private String generateRequestId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Request handler.
   *
   * @param ctx container request context
   */
  @Override
  @Traced(operationName = "RequestLoggingFilter.request-filter")
  public void filter(ContainerRequestContext ctx) {
    ctx.setProperty(LOG_START_TIME_PROPERTY, System.currentTimeMillis());
    String requestId = generateRequestId();
    ctx.setProperty(REQUEST_ID_HEADER, requestId);
    MDC.put(REQUEST_ID_HEADER, requestId);
  }

  /**
   * Response handler.
   *
   * @param request container request context
   * @param response container response context
   */
  @Override
  @Traced(operationName = "RequestLoggingFilter.response-filter")
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {
    int status = response.getStatus();
    Family responseFamily = Family.familyOf(status);
    if (responseFamily == CLIENT_ERROR) {
      metricsService.requestError(status).inc();
      metricsService.requestClientError(status).inc();
    } else if (responseFamily == SERVER_ERROR) {
      metricsService.requestError(status).inc();
      metricsService.requestServerError(status).inc();
    }
    metricsService.requestCount().inc();

    requestId(request)
        .ifPresent(requestId -> response.getHeaders().putSingle(REQUEST_ID_HEADER, requestId));

    logRequestLatency(request, status);
    MDC.clear();
  }

  private void logRequestLatency(ContainerRequestContext request, int status) {
    startTime(request)
        .ifPresent(
            startTime ->
                logRequestLatency(
                    request.getUriInfo().getPath(), request.getMethod(), startTime, status));
  }

  private void logRequestLatency(String path, String method, long startTime, int status) {
    long timeElapsed = System.currentTimeMillis() - startTime;
    metricsService.requestDuration(path, method, status).update(timeElapsed);
    if (timeElapsed > REQUEST_LATENCY_LOG_LIMIT_MS) {
      log.info("Request processing latency: {}ms", timeElapsed);
    }
  }

  private Optional<String> requestId(ContainerRequestContext request) {
    return Optional.ofNullable((String) request.getProperty(REQUEST_ID_HEADER));
  }

  private Optional<Long> startTime(ContainerRequestContext request) {
    return Optional.ofNullable((Long) request.getProperty(LOG_START_TIME_PROPERTY));
  }
}
