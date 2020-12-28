package com.meemaw.shared.context;

import com.meemaw.shared.rest.headers.MissingHttpHeaders;
import java.util.Optional;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

public final class RequestContextUtils {

  private RequestContextUtils() {}

  /**
   * @param resourceInfo resource info
   * @return maybe full resource path
   */
  public static Optional<String> getResourcePath(ResourceInfo resourceInfo) {
    if (resourceInfo == null) {
      return Optional.empty();
    }

    Class<?> resourceClass = resourceInfo.getResourceClass();
    return Optional.ofNullable(resourceClass)
        .map(clazz -> clazz.getAnnotation(Path.class))
        .map(Path::value)
        .map(
            resourcePath -> {
              String methodPath =
                  Optional.ofNullable(resourceInfo.getResourceMethod())
                      .map(m -> m.getAnnotation(Path.class))
                      .map(Path::value)
                      .orElse("");

              return String.join("/", resourcePath, methodPath);
            });
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress,
   * X-Forwarded-* headers are used.
   *
   * @param context ContainerRequestContext
   * @return server base URL
   */
  public static String getServerBaseURL(ContainerRequestContext context) {
    String proto = context.getHeaderString(MissingHttpHeaders.X_FORWARDED_PROTO);
    String host = context.getHeaderString(MissingHttpHeaders.X_FORWARDED_HOST);
    return RequestUtils.getServerBaseUrl(context.getUriInfo(), proto, host);
  }
}
