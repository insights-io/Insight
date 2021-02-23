package com.rebrowse.auth.sso.bearer;

import com.rebrowse.shared.context.RequestContextUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.auth.sso.AbstractAuthDynamicFeature;
import com.rebrowse.auth.sso.AuthScheme;
import com.rebrowse.auth.sso.AuthSchemeResolver;
import com.rebrowse.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature.BearerTokenAuthFilter;
import com.rebrowse.auth.sso.session.model.PrincipalSecurityContext;
import com.rebrowse.auth.user.UserRegistry;
import com.rebrowse.auth.user.model.AuthUser;
import io.opentracing.Span;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
public abstract class AbstractBearerTokenSecurityRequirementAuthDynamicFeature
    extends AbstractAuthDynamicFeature<BearerTokenSecurityRequirement, BearerTokenAuthFilter>
    implements AuthSchemeResolver {

  private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer ([^ ]+)$");

  private static final String MALFORMED_MESSAGE = "[AUTH]: Malformed authorization header";
  private static final String MISSING_MESSAGE = "[AUTH]: Missing authorization header";
  private static final String SERVICE_TO_SERVICE_MESSAGE = "[AUTH]: S2S authorization header";

  @ConfigProperty(name = "authorization.s2s.api.key")
  String s2sApiKey;

  public static String header(String token) {
    return String.format("Bearer %s", token);
  }

  public abstract CompletionStage<Optional<AuthUser>> findUser(String apiKey);

  @Override
  public BearerTokenAuthFilter authFilter(
      BearerTokenSecurityRequirement bearerTokenSecurityRequirement) {
    return new BearerTokenAuthFilter();
  }

  @Override
  public Class<BearerTokenSecurityRequirement> getAnnotation() {
    return BearerTokenSecurityRequirement.class;
  }

  @Override
  @Traced
  public void tryAuthenticate(ContainerRequestContext context) {
    Span span = tracer.activeSpan();
    String authorization = context.getHeaderString(HttpHeaders.AUTHORIZATION);

    if (authorization == null) {
      log.debug(MISSING_MESSAGE);
      span.log(MISSING_MESSAGE);
      throw Boom.unauthorized().exception();
    }

    Matcher matcher = BEARER_PATTERN.matcher(authorization);
    if (!matcher.matches()) {
      log.debug(MALFORMED_MESSAGE);
      span.log(MALFORMED_MESSAGE);
      span.setTag(HttpHeaders.AUTHORIZATION, authorization);
      throw Boom.unauthorized().exception();
    }

    String apiKey = matcher.group(1);
    AuthUser user;
    if (s2sApiKey.equals(apiKey)) {
      user = UserRegistry.S2S_INTERNAL_USER;
      span.log(SERVICE_TO_SERVICE_MESSAGE);
    } else {
      Optional<AuthUser> maybeUser = findUser(apiKey).toCompletableFuture().join();
      user = maybeUser.orElseThrow(() -> Boom.unauthorized().exception());
    }

    setUserContext(span, user);
    boolean isSecure = RequestContextUtils.getServerBaseUri(context).getScheme().equals("https");
    context.setSecurityContext(new PrincipalSecurityContext(user, isSecure));
    principal.user(user).apiKey(apiKey);
    log.debug("[AUTH]: Successfully authenticated user={}", user.getId());
  }

  @Override
  public AuthScheme getAuthScheme() {
    return AuthScheme.BEARER_TOKEN;
  }

  @Priority(Priorities.AUTHENTICATION)
  public class BearerTokenAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) {
      tryAuthenticate(context);
    }
  }
}
