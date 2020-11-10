package com.meemaw.auth.sso;

import com.meemaw.auth.sso.cookie.SsoSessionCookieSecurityRequirementSidecarDynamicFeature;
import com.meemaw.auth.sso.token.BearerTokenSidecarSecurityRequirementAuthDynamicFeature;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SidecarSecurityRequirementDynamicFeature
    extends AbstractSecurityRequirementDynamicFeature {

  @Inject BearerTokenSidecarSecurityRequirementAuthDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject SsoSessionCookieSecurityRequirementSidecarDynamicFeature ssoSessionAuthDynamicFeature;

  @Override
  public Map<AuthScheme, AuthSchemeResolver> initResolvers() {
    return Map.of(
        AuthScheme.SSO_SESSION_COOKIE,
        ssoSessionAuthDynamicFeature,
        AuthScheme.BEARER_TOKEN,
        bearerTokenAuthDynamicFeature);
  }
}
