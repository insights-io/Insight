package com.meemaw.auth.sso;

import com.meemaw.auth.sso.session.cookie.ChallengeSessionCookieSecurityRequirementDynamicFeature;
import com.meemaw.auth.sso.session.cookie.SsoSessionCookieSecurityRequirementDynamicFeature;
import com.meemaw.auth.sso.token.bearer.BearerTokenSecurityRequirementDynamicFeature;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityRequirementDynamicFeature extends AbstractSecurityRequirementDynamicFeature {

  @Inject BearerTokenSecurityRequirementDynamicFeature bearerTokenAuthDynamicFeature;
  @Inject SsoSessionCookieSecurityRequirementDynamicFeature ssoSessionAuthDynamicFeature;

  @Inject
  ChallengeSessionCookieSecurityRequirementDynamicFeature challengeSessionAuthDynamicFeature;

  @Override
  public Map<AuthScheme, AuthSchemeResolver> initResolvers() {
    return Map.of(
        AuthScheme.SSO_SESSION_COOKIE,
        ssoSessionAuthDynamicFeature,
        AuthScheme.CHALLENGE_SESSION_COOKIE,
        challengeSessionAuthDynamicFeature,
        AuthScheme.BEARER_TOKEN,
        bearerTokenAuthDynamicFeature);
  }
}
