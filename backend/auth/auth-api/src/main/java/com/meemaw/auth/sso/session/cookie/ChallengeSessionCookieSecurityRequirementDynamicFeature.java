package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractChallengeSecurityRequirementDynamicFeature;
import com.meemaw.auth.tfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class ChallengeSessionCookieSecurityRequirementDynamicFeature
    extends AbstractChallengeSecurityRequirementDynamicFeature {

  @Inject
  MfaChallengeService challengeService;

  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String cookieValue) {
    return challengeService.retrieveUser(cookieValue);
  }
}
