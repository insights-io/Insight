package com.rebrowse.auth.sso.cookie;

import com.rebrowse.auth.sso.AbstractAuthDynamicFeature;
import com.rebrowse.auth.sso.AuthSchemeResolver;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.sso.session.model.PrincipalSecurityContext;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.context.RequestContextUtils;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.rest.response.Boom;
import io.opentracing.Span;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriInfo;
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

  @Context HttpServerRequest request;
  @Context UriInfo info;

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

  protected abstract NewCookie clearCookie(String domain);

  @Override
  public Class<SessionCookieSecurityRequirement> getAnnotation() {
    return SessionCookieSecurityRequirement.class;
  }

  @Override
  public CookieAuthFilter authFilter(
      SessionCookieSecurityRequirement sessionCookieSecurityRequirement) {
    return new CookieAuthFilter();
  }

  private String getDomain(ContainerRequestContext context) {
    URI serverBaseUri = RequestContextUtils.getServerBaseUri(context);
    return URIUtils.parseCookieDomain(serverBaseUri);
  }

  @Override
  @Traced
  public void tryAuthenticate(ContainerRequestContext context) {

    Span span = tracer.activeSpan();
    Map<String, Cookie> cookies = context.getCookies();
    Cookie sessionCookie = cookies.get(cookieName);

    System.out.println(cookieName);
    System.out.println(sessionCookie);

    if (sessionCookie == null) {
      String message = String.format("[AUTH]: Missing %s cookie", cookieName);
      log.debug(message);
      span.log(message);
      throw Boom.unauthorized().exception();
    }

    String cookieValue = sessionCookie.getValue();
    span.setTag(identifier, cookieValue);
    MDC.put(identifier, cookieValue);

    if (cookieValue.length() != cookieSize) {
      String message =
          String.format(
              "[AUTH]: Invalid %s cookie size expected=%d received=%d",
              cookieName, cookieSize, cookieValue.length());
      log.debug(message);
      span.log(message);
      context.abortWith(
          Boom.unauthorized().responseBuilder().cookie(clearCookie(getDomain(context))).build());
      return;
    }

    Optional<AuthUser> maybeUser = findSession(cookieValue).toCompletableFuture().join();
    if (maybeUser.isEmpty()) {
      System.out.println("USER IS EMPTY!!");
      context.abortWith(
          Boom.unauthorized().responseBuilder().cookie(clearCookie(getDomain(context))).build());
      return;
    }

    AuthUser user = maybeUser.get();
    setUserContext(span, user);
    boolean isSecure = RequestContextUtils.getServerBaseUri(context).getScheme().equals("https");
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
