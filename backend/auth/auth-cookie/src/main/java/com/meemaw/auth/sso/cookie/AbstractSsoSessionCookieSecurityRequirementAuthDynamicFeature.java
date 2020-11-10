package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.shared.logging.LoggingConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSsoSessionCookieSecurityRequirementAuthDynamicFeature
    extends AbstractCookieSecurityRequirementAuthDynamicFeature {

  public AbstractSsoSessionCookieSecurityRequirementAuthDynamicFeature() {
    super(
        SsoSession.COOKIE_NAME,
        SsoSession.SIZE,
        LoggingConstants.SSO_SESSION_ID,
        InsightPrincipal::sessionId);
  }
}
