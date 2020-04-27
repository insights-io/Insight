package com.meemaw.shared.context;

import java.util.Optional;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceInfo;

public final class RequestContextUtils {

  private RequestContextUtils() {
  }

  public static Optional<String> getResourcePath(ResourceInfo resourceInfo) {
    if (resourceInfo == null) {
      return Optional.empty();
    }

    Class<?> resourceClass = resourceInfo.getResourceClass();
    return Optional.ofNullable(resourceClass)
        .map(clazz -> clazz.getAnnotation(Path.class))
        .map(Path::value)
        .map(resourcePath -> {
          String methodPath = Optional.ofNullable(resourceInfo.getResourceMethod())
              .map(m -> m.getAnnotation(Path.class))
              .map(Path::value)
              .orElse("");

          return String.join("/", resourcePath, methodPath);
        });
  }

}
