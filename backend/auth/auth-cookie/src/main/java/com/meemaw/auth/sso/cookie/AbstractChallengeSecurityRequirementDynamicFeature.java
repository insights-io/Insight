package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.shared.logging.LoggingConstants;

public abstract class AbstractChallengeSecurityRequirementDynamicFeature
    extends AbstractCookieSecurityRequirementDynamicFeature {

  public AbstractChallengeSecurityRequirementDynamicFeature() {
    super(
        SsoChallenge.COOKIE_NAME,
        SsoChallenge.SIZE,
        LoggingConstants.CHALLENGE_SESSION_ID,
        InsightPrincipal::challengeId);
  }
}
