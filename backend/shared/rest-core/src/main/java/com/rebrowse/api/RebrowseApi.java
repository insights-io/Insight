package com.rebrowse.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class RebrowseApi {

  public static final Charset CHARSET = StandardCharsets.UTF_8;
  public static final String REQUEST_ID_HEADER = "X-Request-ID";

  private RebrowseApi() {}
}
