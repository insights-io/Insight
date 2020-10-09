package com.rebrowse.util;

import java.util.Objects;
import java.util.regex.Pattern;

public final class StringUtils {

  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

  private StringUtils() {}

  public static boolean containsWhitespace(String str) {
    return WHITESPACE_PATTERN.matcher(Objects.requireNonNull(str)).find();
  }
}
