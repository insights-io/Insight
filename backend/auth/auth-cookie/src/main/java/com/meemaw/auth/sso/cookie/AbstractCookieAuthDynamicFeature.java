package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.AbstractAuthDynamicFeature;
import com.meemaw.auth.sso.session.model.InsightSecurityContext;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestContextUtils;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.response.Boom;
import io.opentracing.Span;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractCookieAuthDynamicFeature
    extends AbstractAuthDynamicFeature<CookieAuth> {

  @Override
  public Class<CookieAuth> getAnnotation() {
    return CookieAuth.class;
  }

  @Priority(Priorities.AUTHENTICATION)
  public abstract class AbstractCookieAuthFilter<T extends AuthUser>
      implements ContainerRequestFilter {

    protected abstract CompletionStage<Optional<T>> findSession(String sessionId);

    @Override
    @Traced(operationName = "AbstractCookieAuthFilter.filter")
    public void filter(ContainerRequestContext context) {
      Map<String, Cookie> cookies = context.getCookies();
      Cookie ssoCookie = cookies.get(SsoSession.COOKIE_NAME);
      if (ssoCookie == null) {
        log.debug("[AUTH]: Missing SessionId");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }

      String sessionId = ssoCookie.getValue();
      if (sessionId.length() != SsoSession.SIZE) {
        log.debug("[AUTH]: Invalid SsoSession size sessionId={}", sessionId);
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }

      MDC.put(LoggingConstants.SSO_SESSION_ID, sessionId);
      Optional<T> maybeUser = findSession(sessionId).toCompletableFuture().join();
      T user = maybeUser.orElseThrow(() -> Boom.status(Status.UNAUTHORIZED).exception());
      Span span = setUserContext(user);
      span.setTag(LoggingConstants.SSO_SESSION_ID, sessionId);
      boolean isSecure = RequestContextUtils.getServerBaseURL(context).startsWith("https");
      context.setSecurityContext(new InsightSecurityContext(user, isSecure));
      principal.user(user);
      log.debug("[AUTH]: Successfully authenticated user={} sessionId={}", user.getId(), sessionId);
    }
  }
}
