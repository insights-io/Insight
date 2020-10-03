package com.meemaw.auth.sso;

import com.meemaw.auth.sso.AbstractAuthenticatedDynamicFeature.AuthenticatedFilter;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

public abstract class AbstractAuthenticatedDynamicFeature
    extends AbstractAuthDynamicFeature<Authenticated, AuthenticatedFilter> {

  public abstract AuthSchemeResolver getCookieAuthSchemeResolver();

  public abstract AuthSchemeResolver getBearerTokenAuthSchemeResolver();

  private Map<AuthScheme, AuthSchemeResolver> authSchemeResolvers;

  @PostConstruct
  public void init() {
    authSchemeResolvers =
        Map.of(
            AuthScheme.COOKIE,
            getCookieAuthSchemeResolver(),
            AuthScheme.BEARER_TOKEN,
            getBearerTokenAuthSchemeResolver());
  }

  @Override
  public Class<Authenticated> getAnnotation() {
    return Authenticated.class;
  }

  @Override
  public AuthenticatedFilter authFilter(Authenticated authenticated) {
    return new AuthenticatedFilter(authenticated);
  }

  @Priority(Priorities.AUTHENTICATION)
  public class AuthenticatedFilter implements ContainerRequestFilter {

    private final Authenticated authenticated;

    public AuthenticatedFilter(Authenticated authenticated) {
      this.authenticated = authenticated;
    }

    @Override
    public void filter(ContainerRequestContext context) {
      BoomException thrownException = Boom.unauthorized().exception();
      AuthScheme[] authSchemes = authenticated.value();

      for (AuthScheme authScheme : authSchemes) {
        AuthSchemeResolver resolver = authSchemeResolvers.get(authScheme);
        try {
          resolver.tryAuthenticate(context);
          return;
        } catch (BoomException boomException) {
          thrownException = boomException;
        }
      }

      throw thrownException;
    }
  }
}
