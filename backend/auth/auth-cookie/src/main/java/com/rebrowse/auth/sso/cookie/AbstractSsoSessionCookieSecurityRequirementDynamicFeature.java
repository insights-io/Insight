package com.rebrowse.auth.sso.cookie;

import com.rebrowse.auth.sso.AuthScheme;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.shared.logging.LoggingConstants;
import javax.ws.rs.core.NewCookie;

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

  @Override
  protected NewCookie clearCookie(String domain) {
    return SsoSession.clearCookie(domain);
  }
}
