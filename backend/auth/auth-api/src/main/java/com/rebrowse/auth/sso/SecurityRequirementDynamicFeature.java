package com.rebrowse.auth.sso;

import com.rebrowse.auth.sso.session.cookie.MfaChallengeSessionCookieSecurityRequirementDynamicFeature;
import com.rebrowse.auth.sso.session.cookie.SsoSessionCookieSecurityRequirementDynamicFeature;
import com.rebrowse.auth.sso.token.bearer.BearerTokenSecurityRequirementDynamicFeature;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityRequirementDynamicFeature extends AbstractSecurityRequirementDynamicFeature {

  @Inject
  BearerTokenSecurityRequirementDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject
  SsoSessionCookieSecurityRequirementDynamicFeature ssoSessionAuthDynamicFeature;

  @Inject
  MfaChallengeSessionCookieSecurityRequirementDynamicFeature challengeSessionAuthDynamicFeature;

  @Override
  public Map<AuthScheme, AuthSchemeResolver> initResolvers() {
    return Map.of(
        AuthScheme.SSO_SESSION_COOKIE,
        ssoSessionAuthDynamicFeature,
        AuthScheme.MFA_CHALLENGE_SESSION_COOKIE,
        challengeSessionAuthDynamicFeature,
        AuthScheme.BEARER_TOKEN,
        bearerTokenAuthDynamicFeature);
  }
}
