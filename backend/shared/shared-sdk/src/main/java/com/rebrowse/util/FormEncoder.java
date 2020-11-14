package com.rebrowse.util;

import com.rebrowse.net.ApiResource;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

public final class FormEncoder {

  public static String createQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .filter(entry -> entry.getValue() instanceof String)
        .map(
            entry ->
                String.format(
                    "%s=%s", urlEncode(entry.getKey()), urlEncode((String) entry.getValue())))
        .collect(Collectors.joining("&"));
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, ApiResource.CHARSET)
        .replaceAll("%5B", "[")
        .replaceAll("%5D", "]");
  }
}
