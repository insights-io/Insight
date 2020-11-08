package com.meemaw.auth.sso;

import com.meemaw.auth.sso.cookie.SessionCookieSecurityRequirementAuthSidecarDynamicFeature;
import com.meemaw.auth.sso.token.BearerTokenSidecarSecurityRequirementAuthDynamicFeature;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityRequirementAuthSidecarDynamicFeature
    extends AbstractSecurityRequirementAuthDynamicFeature {

  @Inject BearerTokenSidecarSecurityRequirementAuthDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject SessionCookieSecurityRequirementAuthSidecarDynamicFeature cookieAuthDynamicFeature;

  @Override
  public AuthSchemeResolver getCookieAuthSchemeResolver() {
    return cookieAuthDynamicFeature;
  }

  @Override
  public AuthSchemeResolver getBearerTokenAuthSchemeResolver() {
    return bearerTokenAuthDynamicFeature;
  }
}
