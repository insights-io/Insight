package com.meemaw.rest.filter;

import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

@Provider
public class RequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "start-time";
    private static final Logger log = LoggerFactory.getLogger(RequestFilter.class);

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext ctx) {
        ctx.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
        MDC.put("requestId", UUID.randomUUID().toString());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        final long timeElapsed = getTimeElapsed(requestContext);
        final String method = requestContext.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();

        log.info("Request {} {} from IP {} took {}ms status {}", method, path, address, timeElapsed, responseContext.getStatus());
    }

    /**
     * Extracts time elapsed from the ContainerRequestContext. Start time in the context might be null if 404 route is
     * requested thus 0L is returned in that case.
     *
     * @param ctx container request context
     * @return elapsed time of the request
     */
    private long getTimeElapsed(ContainerRequestContext ctx) {
        Long startTime = (Long) ctx.getProperty(START_TIME_PROPERTY);
        if (startTime == null) {
            return 0L;
        }
        return System.currentTimeMillis() - startTime;
    }

}
