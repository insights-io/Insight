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
  public static Optional<URL> sneakyParseRefererURL(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader("referer")).map(RequestUtils::sneakyURL);
  }

  /**
   * Extracts referer base URL from http server request if present.
   *
   * @param request http server request
   * @return Optional String base URL as string
   * @throws com.meemaw.shared.rest.exception.BoomException if malformed URL
   */
  public static Optional<String> parseRefererBaseURL(HttpServerRequest request) {
    return sneakyParseRefererURL(request).map(RequestUtils::parseBaseURL);
  }

  public static String sneakyParseBaseURL(String url) {
    return parseBaseURL(sneakyURL(url));
  }

  /**
   * Parses URL with caught checked exception.
   *
   * @param url http request url
   * @return URL
   * @throws com.meemaw.shared.rest.exception.BoomException if malformed URL
   */
  private static URL sneakyURL(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw Boom.badRequest().message(e.getMessage()).exception(e);
    }
  }

  /**
   * Parse base URL.
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
    return getServerBaseURL(info, proto, host);
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress, *
   * X-Forwarded-* headers are used.
   *
   * @param info uri info
   * @param forwardedProto String X-Forwarded-Proto header
   * @param forwardedHost String X-Forwarded-Host header
   * @return server base URL
   */
  public static String getServerBaseURL(UriInfo info, String forwardedProto, String forwardedHost) {
    if (forwardedProto != null && forwardedHost != null) {
      return forwardedProto + "://" + forwardedHost;
    }
    return info.getBaseUri().toString().replaceAll("/$", "");
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
