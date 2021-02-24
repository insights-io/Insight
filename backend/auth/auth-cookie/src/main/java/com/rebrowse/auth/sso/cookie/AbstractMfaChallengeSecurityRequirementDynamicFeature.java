package com.rebrowse.auth.sso.cookie;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.sso.AuthScheme;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.shared.logging.LoggingConstants;

public abstract class AbstractMfaChallengeSecurityRequirementDynamicFeature
    extends AbstractCookieSecurityRequirementDynamicFeature {

  public AbstractMfaChallengeSecurityRequirementDynamicFeature() {
    super(
        AuthorizationMfaChallengeSession.COOKIE_NAME,
        AuthorizationMfaChallengeSession.SIZE,
        LoggingConstants.CHALLENGE_SESSION_ID,
        AuthPrincipal::challengeId);
  }

  @Override
  public AuthScheme getAuthScheme() {
    return AuthScheme.MFA_CHALLENGE_SESSION_COOKIE;
  }
}
