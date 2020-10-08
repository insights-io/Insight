package com.meemaw.auth.sso;

import com.meemaw.auth.sso.session.cookie.SessionCookieSecurityRequirementAuthDynamicFeature;
import com.meemaw.auth.sso.token.bearer.BearerTokenSecurityRequirementAuthDynamicFeature;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityRequirementAuthDynamicFeature
    extends AbstractSecurityRequirementAuthDynamicFeature {

  @Inject BearerTokenSecurityRequirementAuthDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject SessionCookieSecurityRequirementAuthDynamicFeature cookieAuthDynamicFeature;

  @Override
  public AuthSchemeResolver getCookieAuthSchemeResolver() {
    return cookieAuthDynamicFeature;
  }

  @Override
  public AuthSchemeResolver getBearerTokenAuthSchemeResolver() {
    return bearerTokenAuthDynamicFeature;
  }
}