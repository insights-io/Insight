package com.rebrowse.shared.rest.filter;

import static com.rebrowse.api.RebrowseApi.REQUEST_ID_HEADER;
import static javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;

import com.rebrowse.shared.context.RequestContextUtils;
import com.rebrowse.shared.metrics.MetricsService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.vertx.core.http.HttpServerRequest;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
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

  private static final String REQUEST_METHOD = "X-Request-Method";
  private static final String REQUEST_ENDPOINT = "X-Request-Endpoint";
  private static final String LOG_START_TIME_PROPERTY = "X-Start-Time";
  private static final long REQUEST_LATENCY_LOG_LIMIT_MS = 1000;

  @Inject MetricsService metricsService;
  @Inject Tracer tracer;
  @Context UriInfo info;
  @Context HttpServerRequest request;
  @Context ResourceInfo resourceInfo;

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
    String requestId =
        Optional.ofNullable(ctx.getHeaderString(REQUEST_ID_HEADER))
            .orElseGet(this::generateRequestId);

    MDC.put(REQUEST_METHOD, ctx.getMethod());
    MDC.put(REQUEST_ID_HEADER, requestId);
    ctx.setProperty(REQUEST_ID_HEADER, requestId);

    Span span = tracer.activeSpan();
    span.setTag(REQUEST_ID_HEADER, requestId);

    RequestContextUtils.getResourcePath(resourceInfo)
        .ifPresent(
            endpoint -> {
              ctx.setProperty(REQUEST_ENDPOINT, endpoint);
              span.setTag(REQUEST_ENDPOINT, endpoint);
              MDC.put(REQUEST_ENDPOINT, endpoint);
            });
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

    String maybeRequestId = requestId(request);
    if (maybeRequestId != null) {
      response.getHeaders().putSingle(REQUEST_ID_HEADER, maybeRequestId);
      logRequestLatency(request.getMethod(), endpoint(request), startTime(request), status);
    }

    MDC.clear();
  }

  private void logRequestLatency(String method, String path, long startTime, int status) {
    long timeElapsed = System.currentTimeMillis() - startTime;
    metricsService.requestDuration(method, path, status).update(timeElapsed);

    if (timeElapsed > REQUEST_LATENCY_LOG_LIMIT_MS) {
      log.warn("High request processing latency: {}ms", timeElapsed);
    }
  }

  private String endpoint(ContainerRequestContext request) {
    return (String) request.getProperty(REQUEST_ENDPOINT);
  }

  private String requestId(ContainerRequestContext request) {
    return (String) request.getProperty(REQUEST_ID_HEADER);
  }

  private long startTime(ContainerRequestContext request) {
    return (long) request.getProperty(LOG_START_TIME_PROPERTY);
  }
}
