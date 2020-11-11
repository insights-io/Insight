package com.meemaw.auth.sso;

import com.meemaw.auth.sso.AbstractSecurityRequirementDynamicFeature.AuthenticatedFilter;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;

public abstract class AbstractSecurityRequirementDynamicFeature
    extends AbstractAuthDynamicFeature<SecurityRequirements, AuthenticatedFilter> {

  private Map<AuthScheme, AuthSchemeResolver> authSchemeResolvers;

  public abstract Map<AuthScheme, AuthSchemeResolver> initResolvers();

  @PostConstruct
  public void init() {
    authSchemeResolvers = initResolvers();
  }

  @Override
  public Class<SecurityRequirements> getAnnotation() {
    return SecurityRequirements.class;
  }

  @Override
  public AuthenticatedFilter authFilter(SecurityRequirements securityRequirements) {
    return new AuthenticatedFilter(securityRequirements);
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
