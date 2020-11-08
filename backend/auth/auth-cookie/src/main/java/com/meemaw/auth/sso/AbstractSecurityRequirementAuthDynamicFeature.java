package com.meemaw.auth.sso;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

import com.meemaw.auth.sso.AbstractSecurityRequirementAuthDynamicFeature.AuthenticatedFilter;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

public abstract class AbstractSecurityRequirementAuthDynamicFeature
    extends AbstractAuthDynamicFeature<SecurityRequirements, AuthenticatedFilter> {

  public abstract AuthSchemeResolver getCookieAuthSchemeResolver();

  public abstract AuthSchemeResolver getBearerTokenAuthSchemeResolver();

  private Map<AuthScheme, AuthSchemeResolver> authSchemeResolvers;

  @PostConstruct
  public void init() {
    authSchemeResolvers =
        Map.of(
            AuthScheme.SESSION_COOKIE,
            getCookieAuthSchemeResolver(),
            AuthScheme.BEARER_TOKEN,
            getBearerTokenAuthSchemeResolver());
  }

  @Override
  public Class<SecurityRequirements> getAnnotation() {
    return SecurityRequirements.class;
  }

  @Override
  public AuthenticatedFilter authFilter(SecurityRequirements SecurityRequirements) {
    return new AuthenticatedFilter(SecurityRequirements);
  }

  @Priority(Priorities.AUTHENTICATION)
  public class AuthenticatedFilter implements ContainerRequestFilter {

    private final SecurityRequirements requirements;

    public AuthenticatedFilter(SecurityRequirements requirements) {
      this.requirements = requirements;
    }

    @Override
    public void filter(ContainerRequestContext context) {
      BoomException thrownException = Boom.unauthorized().exception();
      SecurityRequirement[] securityRequirements = requirements.value();

      for (SecurityRequirement requirement : securityRequirements) {
        AuthScheme scheme = AuthScheme.fromSecurityRequirement(requirement.name());
        AuthSchemeResolver resolver = authSchemeResolvers.get(scheme);
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
