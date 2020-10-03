package com.meemaw.auth.sso;

import com.meemaw.auth.sso.cookie.CookieAuthSidecarDynamicFeature;
import com.meemaw.auth.sso.token.BearerTokenSidecarAuthDynamicFeature;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticatedSidecarDynamicFeature extends AbstractAuthenticatedDynamicFeature {

  @Inject BearerTokenSidecarAuthDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject CookieAuthSidecarDynamicFeature cookieAuthDynamicFeature;

  @Override
  public AuthSchemeResolver getCookieAuthSchemeResolver() {
    return cookieAuthDynamicFeature;
  }

  @Override
  public AuthSchemeResolver getBearerTokenAuthSchemeResolver() {
    return bearerTokenAuthDynamicFeature;
  }
}
