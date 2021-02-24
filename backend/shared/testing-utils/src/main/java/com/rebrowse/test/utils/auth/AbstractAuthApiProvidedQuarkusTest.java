package com.rebrowse.test.utils.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.test.testconainers.api.auth.AuthApiTestExtension;
import com.rebrowse.test.testconainers.api.auth.AuthApiTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

@QuarkusTestResource(AuthApiTestResource.class)
public abstract class AbstractAuthApiProvidedQuarkusTest {

  private static final Pattern LINK_PATTERN = Pattern.compile("^.*href=\"(http:\\/\\/.*)\".*$");

  @Inject protected ObjectMapper objectMapper;

  private static String findPattern(String[] lines, Pattern pattern) {
    String maybeMatch = null;
    for (String line : lines) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        maybeMatch = matcher.group(1);
      }
    }
    return maybeMatch;
  }

  public SignUpFlows signUpFlows() {
    return new SignUpFlows(authApiBaseUri(), objectMapper, this::confirmSignUpLinkProvider);
  }

  public AuthorizationFlows authorizationFlows() {
    return new AuthorizationFlows(authApiBaseUri(), objectMapper);
  }

  private URI authApiBaseUri() {
    return AuthApiTestExtension.getInstance().getBaseUri();
  }

  private String confirmSignUpLinkProvider(String email) {
    return findPattern(AuthApiTestExtension.getInstance().getLogs().split("\n"), LINK_PATTERN);
  }
}
