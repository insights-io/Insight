package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.shared.logging.LoggingConstants;

public abstract class AbstractSsoSessionCookieSecurityRequirementDynamicFeature
    extends AbstractCookieSecurityRequirementDynamicFeature {

  public AbstractSsoSessionCookieSecurityRequirementDynamicFeature() {
    super(
        SsoSession.COOKIE_NAME,
        SsoSession.SIZE,
        LoggingConstants.SSO_SESSION_ID,
        AuthPrincipal::sessionId);
  }

  @Override
  public AuthScheme getAuthScheme() {
    return AuthScheme.SSO_SESSION_COOKIE;
  }
}
