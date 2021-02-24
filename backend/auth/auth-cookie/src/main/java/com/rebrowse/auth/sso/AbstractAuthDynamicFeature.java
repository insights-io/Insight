package com.rebrowse.auth.sso;

import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.logging.LoggingConstants;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import org.slf4j.MDC;

public abstract class AbstractAuthDynamicFeature<
        T extends Annotation, F extends ContainerRequestFilter>
    implements DynamicFeature {

  @Inject protected AuthPrincipal principal;
  @Inject protected Tracer tracer;

  public abstract Class<T> getAnnotation();

  public abstract F authFilter(T annotation);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    Class<T> annotationClazz = getAnnotation();
    T annotation = resourceInfo.getResourceMethod().getAnnotation(annotationClazz);
    if (annotation == null) {
      annotation = resourceInfo.getResourceClass().getAnnotation(annotationClazz);
    }

    if (annotation != null) {
      context.register(authFilter(annotation));
    }
  }

  protected Span setUserContext(Span span, AuthUser user) {
    String userId = user.getId().toString();
    String role = user.getRole().toString();
    String organizationId = user.getOrganizationId();

    MDC.put(LoggingConstants.USER_ID, userId);
    MDC.put(LoggingConstants.USER_ROLE, role);
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);

    span.setTag(LoggingConstants.USER_ID, userId);
    span.setTag(LoggingConstants.USER_ROLE, role);
    span.setTag(LoggingConstants.ORGANIZATION_ID, organizationId);

    return span;
  }
}
