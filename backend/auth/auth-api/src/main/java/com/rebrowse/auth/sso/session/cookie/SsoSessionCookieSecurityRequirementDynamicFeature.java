package com.rebrowse.auth.sso.session.cookie;

import com.rebrowse.auth.sso.cookie.AbstractSsoSessionCookieSecurityRequirementDynamicFeature;
import com.rebrowse.auth.sso.session.datasource.SsoSessionDatasource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.session.model.SsoUser;
import com.rebrowse.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
public class SsoSessionCookieSecurityRequirementDynamicFeature
    extends AbstractSsoSessionCookieSecurityRequirementDynamicFeature {

  @Inject
  SsoSessionDatasource ssoSessionDatasource;

  @Override
  @Traced
  protected CompletionStage<Optional<AuthUser>> findSession(String cookieValue) {
    return ssoSessionDatasource
        .retrieve(cookieValue)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }

  @Override
  protected NewCookie clearCookie(String domain) {
    return SsoSession.clearCookie(domain);
  }
}
