package com.meemaw.test.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.test.testconainers.api.auth.AuthApiTestExtension;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalAuthApiSsoTestProvider extends AuthApiTestProvider {

  private static final Pattern LINK_PATTERN = Pattern.compile("^.*href=\"(http:\\/\\/.*)\".*$");

  public ExternalAuthApiSsoTestProvider(ObjectMapper objectMapper) {
    super(
        AuthApiTestExtension.getInstance().getBaseURI(),
        objectMapper,
        (email) ->
            findPattern(AuthApiTestExtension.getInstance().getLogs().split("\n"), LINK_PATTERN));
  }

  public static String findPattern(String[] lines, Pattern pattern) {
    String maybeMatch = null;
    for (String line : lines) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        maybeMatch = matcher.group(1);
      }
    }
    return maybeMatch;
  }
}
