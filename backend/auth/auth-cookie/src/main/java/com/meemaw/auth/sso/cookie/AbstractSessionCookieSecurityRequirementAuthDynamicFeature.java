package com.meemaw.auth.sso.cookie;

import io.opentracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

import com.meemaw.auth.sso.AbstractAuthDynamicFeature;
import com.meemaw.auth.sso.AuthSchemeResolver;
import com.meemaw.auth.sso.cookie.AbstractSessionCookieSecurityRequirementAuthDynamicFeature.CookieAuthFilter;
import com.meemaw.auth.sso.session.model.InsightSecurityContext;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestContextUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;

@Slf4j
public abstract class AbstractSessionCookieSecurityRequirementAuthDynamicFeature
    extends AbstractAuthDynamicFeature<SessionCookieSecurityRequirement, CookieAuthFilter>
    implements AuthSchemeResolver {

  protected abstract CompletionStage<Optional<AuthUser>> findSession(String sessionId);

  @Override
  public Class<SessionCookieSecurityRequirement> getAnnotation() {
    return SessionCookieSecurityRequirement.class;
  }

  @Override
  public CookieAuthFilter authFilter(
      SessionCookieSecurityRequirement sessionCookieSecurityRequirement) {
    return new CookieAuthFilter();
  }

  @Priority(Priorities.AUTHENTICATION)
  public class CookieAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) {
      tryAuthenticate(context);
    }
  }

  @Override
  @Traced(operationName = "AbstractCookieAuthDynamicFeature.tryAuthenticate")
  public void tryAuthenticate(ContainerRequestContext context) {
    Span span = tracer.activeSpan();
    Map<String, Cookie> cookies = context.getCookies();
    Cookie ssoCookie = cookies.get(SsoSession.COOKIE_NAME);
    if (ssoCookie == null) {
      log.debug("[AUTH]: Missing SessionId");
      span.log("[CookieAuth]: Missing SessionId");
      throw Boom.unauthorized().exception();
    }

    String sessionId = ssoCookie.getValue();
    span.setTag(LoggingConstants.SSO_SESSION_ID, sessionId);
    MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);

    if (sessionId.length() != SsoSession.SIZE) {
      log.debug("[AUTH]: Invalid SsoSession size sessionId={}", sessionId);
      span.log("[CookieAuth]: Invalid SessionId size");
      throw Boom.unauthorized().exception();
    }

    Optional<AuthUser> maybeUser = findSession(sessionId).toCompletableFuture().join();
    AuthUser user = maybeUser.orElseThrow(() -> Boom.unauthorized().exception());
    setUserContext(span, user);
    boolean isSecure = RequestContextUtils.getServerBaseURL(context).startsWith("https");
    context.setSecurityContext(new InsightSecurityContext(user, isSecure));
    principal.user(user).sessionId(sessionId);
    log.debug("[AUTH]: Successfully authenticated user={} sessionId={}", user.getId(), sessionId);
  }
}
