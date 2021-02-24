package com.rebrowse.shared.context;

import com.rebrowse.shared.rest.exception.BoomException;
import com.rebrowse.shared.rest.headers.MissingHttpHeaders;
import com.rebrowse.shared.rest.response.Boom;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public final class RequestUtils {

  private RequestUtils() {}

  /**
   * Extracts referrer URL from http server request if present.
   *
   * @param request http server request
   * @return Optional URL
   * @throws BoomException if malformed URL
   */
  public static Optional<URI> sneakyParseReferrerUrl(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader(HttpHeaders.REFERER)).map(URI::create);
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress, *
   * X-Forwarded-* headers are used.
   *
   * @param info request uri info
   * @param request vertx http request
   * @return server base URI
   */
  public static URI getServerBaseUri(UriInfo info, HttpServerRequest request) {
    String proto = request.getHeader(MissingHttpHeaders.X_FORWARDED_PROTO);
    String host = request.getHeader(MissingHttpHeaders.X_FORWARDED_HOST);
    return URIUtils.getServerBaseUri(info.getBaseUri(), proto, host);
  }

  /**
   * Extracts referrer base URL from http server request if present.
   *
   * @param request http server request
   * @return Optional String base URL as string
   * @throws BoomException if malformed URL
   */
  public static Optional<URI> parseReferrerOrigin(HttpServerRequest request) {
    return sneakyParseReferrerUrl(request).map(URIUtils::parseOrigin);
  }

  /**
   * Parses URL with caught checked exception.
   *
   * @param url http request url
   * @return URL
   * @throws BoomException if malformed URL
   */
  public static URL sneakyUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw Boom.badRequest().message(e.getMessage()).exception(e);
    }
  }

  public static URI sneakyUri(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw Boom.badRequest().message(e.getMessage()).exception(e);
    }
  }

  /**
   * Converts JAX-RS {@link MultivaluedMap} into a native java {@link Map}.
   *
   * @param map JAX-RS multivalued map
   * @return java Map
   */
  public static Map<String, List<String>> map(MultivaluedMap<String, String> map) {
    return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Entry::getValue));
  }
}
