package com.rebrowse.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

public final class RebrowseApi {

  public static final Charset CHARSET = StandardCharsets.UTF_8;
  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  public static final String REQUEST_ID_HEADER = "X-Request-ID";

  private RebrowseApi() {}
}
