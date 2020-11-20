package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.AbstractAuthDynamicFeature;
import com.meemaw.auth.sso.AuthSchemeResolver;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.session.model.PrincipalSecurityContext;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestContextUtils;
import com.meemaw.shared.rest.response.Boom;
import io.opentracing.Span;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractCookieSecurityRequirementDynamicFeature
    extends AbstractAuthDynamicFeature<
        SessionCookieSecurityRequirement,
        AbstractCookieSecurityRequirementDynamicFeature.CookieAuthFilter>
    implements AuthSchemeResolver {

  private final String cookieName;
  private final int cookieSize;
  private final String identifier;
  private final BiFunction<AuthPrincipal, String, AuthPrincipal> cookieProvider;

  public AbstractCookieSecurityRequirementDynamicFeature(
      String cookieName,
      int cookieSize,
      String identifier,
      BiFunction<AuthPrincipal, String, AuthPrincipal> cookieProvider) {
    this.cookieName = cookieName;
    this.cookieSize = cookieSize;
    this.identifier = identifier;
    this.cookieProvider = cookieProvider;
  }

  protected abstract CompletionStage<Optional<AuthUser>> findSession(String cookieValue);

  @Override
  public Class<SessionCookieSecurityRequirement> getAnnotation() {
    return SessionCookieSecurityRequirement.class;
  }

  @Override
  public CookieAuthFilter authFilter(
      SessionCookieSecurityRequirement sessionCookieSecurityRequirement) {
    return new CookieAuthFilter();
  }

  @Override
  @Traced(operationName = "AbstractCookieSecurityRequirementAuthDynamicFeature.tryAuthenticate")
  public void tryAuthenticate(ContainerRequestContext context) {
    Span span = tracer.activeSpan();
    Map<String, Cookie> cookies = context.getCookies();
    Cookie ssoCookie = cookies.get(cookieName);
    if (ssoCookie == null) {
      String message = String.format("[AUTH]: Missing %s cookie", cookieName);
      log.debug(message);
      span.log(message);
      throw Boom.unauthorized().exception();
    }

    String cookieValue = ssoCookie.getValue();
    span.setTag(identifier, cookieValue);
    MDC.put(identifier, cookieValue);

    if (cookieValue.length() != cookieSize) {
      String message =
          String.format(
              "[AUTH]: Invalid %s cookie size expected=%d received=%d",
              cookieName, cookieSize, cookieValue.length());
      log.debug(message);
      span.log(message);
      throw Boom.unauthorized().exception();
    }

    Optional<AuthUser> maybeUser = findSession(cookieValue).toCompletableFuture().join();
    AuthUser user = maybeUser.orElseThrow(() -> Boom.unauthorized().exception());
    setUserContext(span, user);
    boolean isSecure = RequestContextUtils.getServerBaseURL(context).startsWith("https");
    context.setSecurityContext(new PrincipalSecurityContext(user, isSecure));
    cookieProvider.apply(principal.user(user), cookieValue);
    log.debug(
        "[AUTH]: Successfully authenticated user={} {}={}", user.getId(), cookieName, cookieValue);
  }

  @Priority(Priorities.AUTHENTICATION)
  public class CookieAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) {
      tryAuthenticate(context);
    }
  }
}
