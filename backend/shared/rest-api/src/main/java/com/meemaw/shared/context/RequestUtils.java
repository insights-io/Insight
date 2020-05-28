package com.meemaw.shared.context;

import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public final class RequestUtils {

  private RequestUtils() {}

  /**
   * Extracts referer base URL from http server request if present.
   *
   * @param request http server request
   * @return Optional String base URL as string
   * @throws com.meemaw.shared.rest.exception.BoomException if malformed URL
   */
  public static Optional<String> parseRefererBaseURL(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader("referer"))
        .map(
            referer -> {
              try {
                return new URL(referer);
              } catch (MalformedURLException e) {
                throw Boom.badRequest().message(e.getMessage()).exception(e);
              }
            })
        .map(RequestUtils::parseBaseURL);
  }

  /**
   * Parse base URL from an URL.
   *
   * @param url URL
   * @return String base url
   */
  public static String parseBaseURL(URL url) {
    String base = url.getProtocol() + "://" + url.getHost();
    if (url.getPort() == -1) {
      return base;
    }
    return base + ":" + url.getPort();
  }
}
