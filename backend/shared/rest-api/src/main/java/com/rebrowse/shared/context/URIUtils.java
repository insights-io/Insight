package com.rebrowse.shared.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

public final class URIUtils {

  private static final int DOMAIN_MIN_PARTS = 2;

  private URIUtils() {}

  public static URI parseOrigin(URL url) throws URISyntaxException {
    return parseOrigin(url.toURI());
  }

  /**
   * Parse URI origin
   *
   * @param uri URI
   * @return URI origin
   */
  public static URI parseOrigin(URI uri) {
    String base = uri.getScheme() + "://" + uri.getHost();
    if (uri.getPort() == -1) {
      return URI.create(base);
    }
    return URI.create(base + ":" + uri.getPort());
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress, *
   * X-Forwarded-* headers are used.
   *
   * @param baseUri internal base uri
   * @param forwardedProto X-Forwarded-Proto header value
   * @param forwardedHost X-Forwarded-Host header value
   * @return server base URI
   */
  public static URI getServerBaseUri(
      URI baseUri, @Nullable String forwardedProto, @Nullable String forwardedHost) {
    if (forwardedProto != null && forwardedHost != null) {
      return UriBuilder.fromUri(forwardedProto + "://" + forwardedHost).build();
    }
    return baseUri;
  }

  public static String removeTrailingSlash(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    if (value.charAt(value.length() - 1) == '/') {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }

  /**
   * Parses top level domain of given URI
   *
   * @param uri associated with the http request
   * @return Optional String top level domain
   */
  public static Optional<String> parseTopLevelDomain(URI uri) {
    String[] parts = Optional.ofNullable(uri.getHost()).orElse("").split("\\.");
    if (parts.length < DOMAIN_MIN_PARTS) {
      return Optional.empty();
    }
    return Optional.of(String.join(".", parts[parts.length - 2], parts[parts.length - 1]));
  }

  public static String parseCookieDomain(URI uri) {
    return parseTopLevelDomain(uri).orElse(null);
  }
}
