package com.meemaw.shared.rest.filter;

import static javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;

import com.meemaw.shared.metrics.MetricsService;
import io.vertx.core.http.HttpServerRequest;
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
  private String requestId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Request handler.
   *
   * @param ctx container request context
   */
  @Override
  @Traced
  public void filter(ContainerRequestContext ctx) {
    ctx.setProperty(LOG_START_TIME_PROPERTY, System.currentTimeMillis());
    String requestId = requestId();
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
  @Traced
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {
    String requestId = (String) request.getProperty(REQUEST_ID_HEADER);
    int status = response.getStatus();
    logRequestLatency(request, status);

    if (requestId != null) {
      response.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);
    }

    Family responseFamily = Family.familyOf(status);
    if (responseFamily == CLIENT_ERROR) {
      metricsService.requestError(status).inc();
      metricsService.requestClientError(status).inc();
    } else if (responseFamily == SERVER_ERROR) {
      metricsService.requestError(status).inc();
      metricsService.requestServerError(status).inc();
    }

    metricsService.requestCount().inc();
    MDC.clear();
  }

  @Traced
  private void logRequestLatency(ContainerRequestContext request, int status) {
    String path = request.getUriInfo().getPath();
    String method = request.getMethod();
    Long startTime = (Long) request.getProperty(LOG_START_TIME_PROPERTY);

    // paths that do not match any JAX-RS endpoint
    if (startTime == null) {
      log.trace("No start time for request {} {}", method, path);
      return;
    }

    long timeElapsed = System.currentTimeMillis() - startTime;
    if (timeElapsed > REQUEST_LATENCY_LOG_LIMIT_MS) {
      log.info("Request processing latency: {}ms", timeElapsed);
    }
    metricsService.requestDuration(path, method, status).update(timeElapsed);
  }
}
