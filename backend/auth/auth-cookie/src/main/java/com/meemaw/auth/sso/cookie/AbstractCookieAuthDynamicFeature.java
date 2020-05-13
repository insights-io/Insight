package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.model.InsightSecurityContext;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractCookieAuthDynamicFeature implements DynamicFeature {

  @Inject InsightPrincipal principal;

  protected abstract ContainerRequestFilter cookieAuthFilter();

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    CookieAuth annotation = resourceInfo.getResourceMethod().getAnnotation(CookieAuth.class);
    if (annotation == null) {
      annotation = resourceInfo.getResourceClass().getAnnotation(CookieAuth.class);
    }

    if (annotation != null) {
      context.register(cookieAuthFilter());
    }
  }

  @Priority(Priorities.AUTHENTICATION)
  public abstract class AbstractCookieAuthFilter<T extends AuthUser>
      implements ContainerRequestFilter {

    protected abstract CompletionStage<Optional<T>> findSession(String sessionId);

    @Override
    public void filter(ContainerRequestContext ctx) {
      Map<String, Cookie> cookies = ctx.getCookies();
      Cookie ssoCookie = cookies.get(SsoSession.COOKIE_NAME);
      if (ssoCookie == null) {
        log.debug("Missing SessionId");
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }

      String sessionId = ssoCookie.getValue();
      if (sessionId.length() != SsoSession.SIZE) {
        log.debug("Invalid SessionId size {}", sessionId);
        throw Boom.status(Status.UNAUTHORIZED).exception();
      }

      Optional<T> maybeUser = findSession(sessionId).toCompletableFuture().join();
      T authUser = maybeUser.orElseThrow(() -> Boom.status(Status.UNAUTHORIZED).exception());
      boolean isSecure = ctx.getUriInfo().getAbsolutePath().toString().startsWith("https");
      ctx.setSecurityContext(new InsightSecurityContext(authUser, isSecure));
      principal.as(authUser);
      MDC.put("user.id", authUser.getId().toString());
      MDC.put("user.org", authUser.getOrg());
      MDC.put("user.role", authUser.getRole().toString());
    }
  }
}
