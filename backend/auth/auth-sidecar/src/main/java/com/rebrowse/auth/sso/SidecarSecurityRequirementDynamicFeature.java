package com.rebrowse.auth.sso;

import com.rebrowse.auth.sso.cookie.SsoSessionCookieSecurityRequirementSidecarDynamicFeature;
import com.rebrowse.auth.sso.token.BearerTokenSidecarSecurityRequirementAuthDynamicFeature;
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
