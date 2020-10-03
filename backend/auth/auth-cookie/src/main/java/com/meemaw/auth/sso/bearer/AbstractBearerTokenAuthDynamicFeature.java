package com.meemaw.auth.sso.bearer;

import com.meemaw.auth.sso.AbstractAuthDynamicFeature;
import com.meemaw.auth.sso.bearer.AbstractBearerTokenAuthDynamicFeature.AbstractBearerTokenAuthFilter;
import com.meemaw.auth.sso.session.model.InsightSecurityContext;
import com.meemaw.auth.user.UserRegistry;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestContextUtils;
import com.meemaw.shared.rest.response.Boom;
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
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Slf4j
public abstract class AbstractBearerTokenAuthDynamicFeature
    extends AbstractAuthDynamicFeature<BearerTokenAuth, AbstractBearerTokenAuthFilter> {

  private static final Pattern BEARER_PATTERN = Pattern.compile("^Bearer ([^ ]+)$");

  @ConfigProperty(name = "authorization.s2s.auth.token")
  String s2sAuthToken;

  public static String header(String token) {
    return String.format("Bearer %s", token);
  }

  @Override
  public Class<BearerTokenAuth> getAnnotation() {
    return BearerTokenAuth.class;
  }

  @Priority(Priorities.AUTHENTICATION)
  public abstract class AbstractBearerTokenAuthFilter implements ContainerRequestFilter {

    public abstract CompletionStage<Optional<AuthUser>> findUser(String token);

    @Override
    @Traced(operationName = "AbstractBearerTokenAuthDynamicFeature.filter")
    public void filter(ContainerRequestContext context) {
      Span span = tracer.activeSpan();
      String authorization = context.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authorization == null) {
        log.debug("[AUTH]: Missing authorization header");
        span.log("[BearerTokenAuth]: Missing authorization header");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }
      Matcher matcher = BEARER_PATTERN.matcher(authorization);
      if (!matcher.matches()) {
        log.debug("[AUTH]: Malformed authorization header");
        span.setTag(HttpHeaders.AUTHORIZATION, authorization);
        span.log("[BearerTokenAuth]: Malformed authorization header");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }
      String token = matcher.group(1);
      AuthUser user;
      if (s2sAuthToken.equals(token)) {
        user = UserRegistry.S2S_INTERNAL_USER;
        span.log("[BearerTokenAuth]: S2S Request");
      } else {
        Optional<AuthUser> maybeUser = findUser(token).toCompletableFuture().join();
        user = maybeUser.orElseThrow(() -> Boom.status(Status.UNAUTHORIZED).exception());
      }

      setUserContext(span, user);
      boolean isSecure = RequestContextUtils.getServerBaseURL(context).startsWith("https");
      context.setSecurityContext(new InsightSecurityContext(user, isSecure));
      principal.user(user);
      log.debug("[AUTH]: Successfully authenticated user={}", user.getId());
    }
  }
}