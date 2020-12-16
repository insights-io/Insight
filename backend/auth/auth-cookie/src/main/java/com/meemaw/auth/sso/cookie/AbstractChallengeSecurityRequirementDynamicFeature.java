package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.shared.logging.LoggingConstants;

public abstract class AbstractChallengeSecurityRequirementDynamicFeature
    extends AbstractCookieSecurityRequirementDynamicFeature {

  public AbstractChallengeSecurityRequirementDynamicFeature() {
    super(
        SsoChallenge.COOKIE_NAME,
        SsoChallenge.SIZE,
        LoggingConstants.CHALLENGE_SESSION_ID,
        AuthPrincipal::challengeId);
  }

  @Override
  public AuthScheme getAuthScheme() {
    return AuthScheme.CHALLENGE_SESSION_COOKIE;
  }
}
