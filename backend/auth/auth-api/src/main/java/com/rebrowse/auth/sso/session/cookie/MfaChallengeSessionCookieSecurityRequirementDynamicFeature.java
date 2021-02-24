package com.rebrowse.auth.sso.session.cookie;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.mfa.setup.service.MfaAuthorizationChallengeService;
import com.rebrowse.auth.sso.cookie.AbstractMfaChallengeSecurityRequirementDynamicFeature;
import com.rebrowse.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;

@Provider
public class MfaChallengeSessionCookieSecurityRequirementDynamicFeature
    extends AbstractMfaChallengeSecurityRequirementDynamicFeature {

  @Inject MfaAuthorizationChallengeService mfaAuthorizationChallengeService;

  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String challengeId) {
    return mfaAuthorizationChallengeService.retrieveChallengedUser(challengeId);
  }

  @Override
  protected NewCookie clearCookie(String domain) {
    return AuthorizationMfaChallengeSession.clearCookie(domain);
  }
}
