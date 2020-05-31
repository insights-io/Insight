package com.meemaw.shared.context;

import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import javax.ws.rs.core.UriInfo;

public final class RequestUtils {

  private static final int DOMAIN_MIN_PARTS = 2;

  private RequestUtils() {}

  /**
   * Extracts referer URL from http server request if present.
   *
   * @param request http server request
   * @return Optional URL
   * @throws com.meemaw.shared.rest.exception.BoomException if malformed URL
   */
  public static Optional<URL> parseRefererURL(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader("referer"))
        .map(
            referer -> {
              try {
                return new URL(referer);
              } catch (MalformedURLException e) {
                throw Boom.badRequest().message(e.getMessage()).exception(e);
              }
            });
  }

  /**
   * Extracts referer base URL from http server request if present.
   *
   * @param request http server request
   * @return Optional String base URL as string
   * @throws com.meemaw.shared.rest.exception.BoomException if malformed URL
   */
  public static Optional<String> parseRefererBaseURL(HttpServerRequest request) {
    return parseRefererURL(request).map(RequestUtils::parseBaseURL);
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

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress,
   * X-Forwarded-* headers are used.
   *
   * @param info uri info
   * @param request http server request
   * @return server base URL
   */
  public static String getServerBaseURL(UriInfo info, HttpServerRequest request) {
    String proto = request.getHeader("X-Forwarded-Proto");
    String host = request.getHeader("X-Forwarded-Host");

    if (proto != null && host != null) {
      return proto + "://" + host;
    }
    return info.getBaseUri().toString();
  }

  /**
   * Parses top level domain of given URL.
   *
   * @param url associated with the http request
   * @return Optional String top level domain
   */
  public static Optional<String> parseTLD(String url) {
    try {
      String[] parts = new URL(url).getHost().split("\\.");
      if (parts.length < DOMAIN_MIN_PARTS) {
        return Optional.empty();
      }
      return Optional.of(String.join(".", parts[parts.length - 2], parts[parts.length - 1]));
    } catch (MalformedURLException e) {
      return Optional.empty();
    }
  }

  /**
   * Parses top level cookie domain of a given URL.
   *
   * @param url associated with the http request
   * @return String cookie domain if present else null
   */
  public static String parseCookieDomain(String url) {
    return parseTLD(url).orElse(null);
  }
}
