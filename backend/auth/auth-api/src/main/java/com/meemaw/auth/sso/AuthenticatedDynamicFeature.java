package com.meemaw.auth.sso;

import com.meemaw.auth.sso.session.cookie.CookieAuthDynamicFeature;
import com.meemaw.auth.sso.token.bearer.BearerTokenAuthDynamicFeature;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticatedDynamicFeature extends AbstractAuthenticatedDynamicFeature {

  @Inject BearerTokenAuthDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject CookieAuthDynamicFeature cookieAuthDynamicFeature;

  @Override
  public AuthSchemeResolver getCookieAuthSchemeResolver() {
    return cookieAuthDynamicFeature;
  }

  @Override
  public AuthSchemeResolver getBearerTokenAuthSchemeResolver() {
    return bearerTokenAuthDynamicFeature;
  }
}
